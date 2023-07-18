package servletcontainer.routes;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Default servlet that is returned when page is not found.
public class DefaultServlet extends  HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(404);
    }
}
