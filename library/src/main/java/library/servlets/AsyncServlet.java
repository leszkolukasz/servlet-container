package library.servlets;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Future;

import javax.servlet.AsyncContext;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

import static java.time.temporal.ChronoUnit.SECONDS;

@WebServlet(
        value = "/async"
)
public class AsyncServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        AsyncContext asyncContext = request.startAsync();
        HttpRequest requestAsync;

        try {
            requestAsync = HttpRequest.newBuilder()
                    .uri(new URI("https://postman-echo.com/get"))
                    .timeout(Duration.of(10, SECONDS))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        Future<HttpResponse<String>> responseAsync = HttpClient.newBuilder()
                .build()
                .sendAsync(requestAsync, HttpResponse.BodyHandlers.ofString());

//        responseAsync.whenComplete((stringHttpResponse, throwable) -> {
//            try {
//                response.getOutputStream().println("Async finished!");
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//                asyncContext.complete();
//        });

//        asyncContext.setTimeout(1000);
        asyncContext.start(() -> {

//            try {
//                Thread.sleep(10000);
//                response.getOutputStream().println("Async finished!");
//            } catch (IOException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }
            asyncContext.complete();
        });
    }
}