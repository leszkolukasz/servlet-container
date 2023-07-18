package servletcontainer.http;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

// Wrapper for HttpServlet with a similar interface as HttpServlet.
// It is mainly necessary for more control over `service` method.
public class HttpServletDelegator {
    final private HttpServlet httpServlet;

    public HttpServletDelegator(HttpServlet httpServlet) {
        this.httpServlet = httpServlet;
    }

    public void service(HttpServletRequestImp req, HttpServletResponseImp resp) {
        dispatchRequest(req, resp);
        if (!req.isAsyncStarted())
            req.complete();
    }

    public void dispatchRequest(HttpServletRequestImp req, HttpServletResponseImp resp) {
        switch (req.getMethod()) {
            case "GET" -> doGet(req, resp);
            case "POST" -> doPost(req, resp);
            case "PUT" -> doPut(req, resp);
            case "DELETE" -> doDelete(req, resp);
            default -> throw new RuntimeException("Unknown HTTP method");
        }
    }

    // do* must be called using Java Reflection because it is protected method.
    public void doGet(HttpServletRequestImp req, HttpServletResponseImp resp) {
        try {
            Method method = httpServlet
                    .getClass()
                    .getDeclaredMethod("doGet", HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(httpServlet, req, resp);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doPost(HttpServletRequestImp req, HttpServletResponseImp resp) {
        try {
            Method method = httpServlet
                    .getClass()
                    .getDeclaredMethod("doPost", HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(httpServlet, req, resp);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doPut(HttpServletRequestImp req, HttpServletResponseImp resp) {
        try {
            Method method = httpServlet
                    .getClass()
                    .getDeclaredMethod("doPut", HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(httpServlet, req, resp);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void doDelete(HttpServletRequestImp req, HttpServletResponseImp resp) {
        try {
            Method method = httpServlet
                    .getClass()
                    .getDeclaredMethod("doDelete", HttpServletRequest.class, HttpServletResponse.class);
            method.setAccessible(true);
            method.invoke(httpServlet, req, resp);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}