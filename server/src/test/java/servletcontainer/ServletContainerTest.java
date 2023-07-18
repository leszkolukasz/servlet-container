package servletcontainer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import servletcontainer.servlet.ServletContainer;
import servletcontainer.servlets.SlowServlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ServletContainerTest {

    private Thread serverThread;
    private ServletContainer servletContainer;

    @AfterEach
    void tearDown() throws InterruptedException {
        servletContainer.stop();
        Thread.sleep(1500);
    }

    void startServer(int port) throws InterruptedException {
        serverThread = new Thread(() -> {
            assertDoesNotThrow(() -> {servletContainer.start(port);});
        });

        serverThread.start();
        Thread.sleep(2000);
    }

    private void makeRequest(HttpClient client, HttpRequest request) {
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void multithreading() throws URISyntaxException, IOException, InterruptedException {
        servletContainer = new ServletContainer(2);
        servletContainer.addRoute(SlowServlet.class, "/slow");
        startServer(1234);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1234/slow"))
                .GET()
                .build();

        var client = HttpClient.newBuilder()
                .build();

        assertTimeoutPreemptively(Duration.ofMillis(6000), () -> {
            Thread t1 = new Thread(() -> {makeRequest(client, request);});
            Thread t2 = new Thread(() -> {makeRequest(client, request);});

            t1.start();
            t2.start();
            t1.join();
            t2.join();
        });

        Thread t1 = new Thread(() -> {makeRequest(client, request);});
        Thread t2 = new Thread(() -> {makeRequest(client, request);});
        Thread t3 = new Thread(() -> {makeRequest(client, request);});

        long start = System.currentTimeMillis();
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        assertTrue(System.currentTimeMillis()-start >= 10000);
    }

    @Test
    void notFound() throws InterruptedException, URISyntaxException, IOException {
        servletContainer = new ServletContainer(2);
        startServer(1235);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1235/notfound"))
                .GET()
                .build();

        var client = HttpClient.newBuilder()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }
}