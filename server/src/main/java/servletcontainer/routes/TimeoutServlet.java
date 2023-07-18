package servletcontainer.routes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Returns timeout message.
public class TimeoutServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setStatus(408);
        resp.resetBuffer();
        resp.getWriter().println("TIMEOUT FOR REQUEST");
    }
}
