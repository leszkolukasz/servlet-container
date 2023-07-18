package servletcontainer.async;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import servletcontainer.servlet.ServletContainer;
import servletcontainer.utils.AsyncTimeoutServlet;
import servletcontainer.utils.EmptyServlet;
import servletcontainer.utils.SlowAsyncServlet;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AsyncServletTest {

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

    private HttpResponse<String> makeRequest(HttpClient client, HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Test
    void testAsyncServlet() throws InterruptedException {
        // It is important that this server uses 1 thread.
        servletContainer = new ServletContainer(1);
        servletContainer.addRoute(SlowAsyncServlet.class, "/slowAsync");
        servletContainer.addRoute(EmptyServlet.class, "/empty");
        startServer(1241);

        HttpClient client = HttpClient.newHttpClient();

        // Make request that will finish in 2 seconds. But this request will not
        // block server because SlowAsyncServlet is async.
        CompletableFuture<Void> asyncRequestFuture = CompletableFuture.runAsync(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:1241/slowAsync"))
                    .build();
            makeRequest(client, request);
        });

        // Make the synchronous request that will finish before previous one.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1241/empty"))
                .build();

        var reponse = makeRequest(client, request);
        assertEquals("", reponse.body());
        assertFalse(asyncRequestFuture.isDone());

        asyncRequestFuture.join();
    }

    @Test
    void testAsyncServletTimeout() throws InterruptedException {
        servletContainer = new ServletContainer(1);
        servletContainer.addRoute(AsyncTimeoutServlet.class, "/asyncTimeout");
        startServer(1242);

        HttpClient client = HttpClient.newHttpClient();

        // This servlet will set async timeout to 1s and will sleep for 2s.
        // It should return message if timeout occurred or nothing otherwise.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:1242/asyncTimeout"))
                .build();

        var reponse = makeRequest(client, request);
        assertEquals("ASYNC TIMEOUT", reponse.body().strip());
    }
}