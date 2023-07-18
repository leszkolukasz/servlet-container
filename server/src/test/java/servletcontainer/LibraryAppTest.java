package servletcontainer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import servletcontainer.servlet.ServletContainer;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class LibraryAppTest {

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
    void bookShow() throws URISyntaxException, InterruptedException {
        servletContainer = new ServletContainer(1);
        servletContainer.servletScan("src/test/resources/deploy");
        startServer(1236);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1236/library/books/show"))
                .GET()
                .build();

        var client = HttpClient.newBuilder()
                .build();

        var reponse = makeRequest(client, request);
        String expected = "<html>\n" +
                "<head>\n" +
                "    <title>Book Table</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<table>\n" +
                "    <thead>\n" +
                "    <tr>\n" +
                "        <th>ID</th>\n" +
                "        <th>Name</th>\n" +
                "        <th>Author</th>\n" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "        </tbody>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";

        assertEquals(expected, reponse.body().strip());
    }

    void addSampleBook(int port, HttpClient client) throws URISyntaxException {
        String requestBody = "name=testName&author=testAuthor";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:" + port + "/library/books/add"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        makeRequest(client, request);
    }

    @Test
    void bookAddGET() throws URISyntaxException, InterruptedException {
        servletContainer = new ServletContainer(1);
        servletContainer.servletScan("src/test/resources/deploy");
        startServer(1237);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1237/library/books/add"))
                .GET()
                .build();

        var client = HttpClient.newBuilder()
                .build();

        var reponse = makeRequest(client, request);

        String expected = "<html>\n" +
                "<head>\n" +
                "    <title>Add Book</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Add Book</h1>\n" +
                "<form action=\"/library/books/add\" method=\"post\">\n" +
                "    <label for=\"name\">Name:</label>\n" +
                "    <input type=\"text\" id=\"name\" name=\"name\" required><br>\n" +
                "    <label for=\"author\">Author:</label>\n" +
                "    <input type=\"text\" id=\"author\" name=\"author\" required><br>\n" +
                "    <input type=\"submit\" value=\"Add Book\">\n" +
                "</form>\n" +
                "</body>\n" +
                "</html>";

        assertEquals(expected, reponse.body().strip());
    }

    @Test
    void bookAddPOST() throws URISyntaxException, InterruptedException {
        servletContainer = new ServletContainer(1);
        servletContainer.servletScan("src/test/resources/deploy");
        startServer(1238);

        var client = HttpClient.newBuilder()
                .build();

        addSampleBook(1238, client);

        var request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1238/library/books/show"))
                .GET()
                .build();

        var reponse = makeRequest(client, request);

        String expected = "<html>\n" +
                "<head>\n" +
                "    <title>Book Table</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<table>\n" +
                "    <thead>\n" +
                "    <tr>\n" +
                "        <th>ID</th>\n" +
                "        <th>Name</th>\n" +
                "        <th>Author</th>\n" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "        <tr>\n" +
                "        <td>0</td>\n" +
                "        <td>testName</td>\n" +
                "        <td>testAuthor</td>\n" +
                "    </tr>\n" +
                "        </tbody>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";

        assertEquals(expected, reponse.body().strip());
    }

    @Test
    void bookUpdateGET() throws URISyntaxException, InterruptedException {
        servletContainer = new ServletContainer(1);
        servletContainer.servletScan("src/test/resources/deploy");
        startServer(1239);

        var client = HttpClient.newBuilder()
                .build();

        addSampleBook(1239, client);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1239/library/books/update?id=0"))
                .GET()
                .build();

        var reponse = makeRequest(client, request);

        String expected = "<html>\n" +
                "<head>\n" +
                "  <title>Update Book</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Update Book</h1>\n" +
                "<form action=\"/library/books/update\" method=\"post\">\n" +
                "  <input type=\"hidden\" name=\"id\" value=\"0\">\n" +
                "  <label for=\"name\">Name:</label>\n" +
                "  <input type=\"text\" id=\"name\" name=\"name\" value=\"testName\"><br>\n" +
                "  <label for=\"author\">Author:</label>\n" +
                "  <input type=\"text\" id=\"author\" name=\"author\" value=\"testAuthor\"><br>\n" +
                "  <input type=\"submit\" value=\"Update\">\n" +
                "</form>\n" +
                "</body>\n" +
                "</html>";

        assertEquals(expected, reponse.body().strip());
    }

    @Test
    void bookUpdatePOST() throws URISyntaxException, InterruptedException {
        servletContainer = new ServletContainer(1);
        servletContainer.servletScan("src/test/resources/deploy");
        startServer(1240);

        var client = HttpClient.newBuilder()
                .build();

        addSampleBook(1240, client);

        // Update book
        String requestBody = "id=0&name=newTestName&author=newTestAuthor";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1240/library/books/update"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        makeRequest(client, request);

        // Show books
        request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1240/library/books/show"))
                .GET()
                .build();

        var reponse = makeRequest(client, request);

        String expected = "<html>\n" +
                "<head>\n" +
                "    <title>Book Table</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<table>\n" +
                "    <thead>\n" +
                "    <tr>\n" +
                "        <th>ID</th>\n" +
                "        <th>Name</th>\n" +
                "        <th>Author</th>\n" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "        <tr>\n" +
                "        <td>0</td>\n" +
                "        <td>newTestName</td>\n" +
                "        <td>newTestAuthor</td>\n" +
                "    </tr>\n" +
                "        </tbody>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";

        assertEquals(expected, reponse.body().strip());
    }

    @Test
    void bookDeleteGET() throws URISyntaxException, InterruptedException {
        servletContainer = new ServletContainer(1);
        servletContainer.servletScan("src/test/resources/deploy");
        startServer(1241);

        var client = HttpClient.newBuilder()
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1241/library/books/delete"))
                .GET()
                .build();

        var reponse = makeRequest(client, request);

        String expected = "<html>\n" +
                "<head>\n" +
                "    <title>Delete Book</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Delete Book</h1>\n" +
                "<form action=\"/library/books/delete\" method=\"post\">\n" +
                "    <label for=\"id\">Name:</label>\n" +
                "    <input type=\"text\" id=\"id\" name=\"id\" required><br>\n" +
                "    <input type=\"submit\" value=\"Delete Book\">\n" +
                "</form>\n" +
                "</body>\n" +
                "</html>";

        assertEquals(expected, reponse.body().strip());
    }

    @Test
    void bookDeletePOST() throws URISyntaxException, InterruptedException {
        servletContainer = new ServletContainer(1);
        servletContainer.servletScan("src/test/resources/deploy");
        startServer(1242);

        var client = HttpClient.newBuilder()
                .build();

        addSampleBook(1242, client);

        // Delete book
        String requestBody = "id=0";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1242/library/books/delete"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        makeRequest(client, request);

        // Show books
        request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:1242/library/books/show"))
                .GET()
                .build();

        var reponse = makeRequest(client, request);

        String expected = "<html>\n" +
                "<head>\n" +
                "    <title>Book Table</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<table>\n" +
                "    <thead>\n" +
                "    <tr>\n" +
                "        <th>ID</th>\n" +
                "        <th>Name</th>\n" +
                "        <th>Author</th>\n" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody>\n" +
                "        </tbody>\n" +
                "</table>\n" +
                "</body>\n" +
                "</html>";

        assertEquals(expected, reponse.body().strip());
    }
}