package servletcontainer.servlet;

import servletcontainer.http.HttpServletDelegator;

import javax.servlet.http.HttpServlet;
import java.lang.reflect.InvocationTargetException;

// Contains metadata about servlet like its url.
public class ServletWrapper {
    private final Class<? extends HttpServlet> cls;
    private final String url;
    private HttpServletDelegator instance;

    public ServletWrapper(Class<? extends HttpServlet> cls, String url) {
        this.cls = cls;
        this.url = url;
        this.instance = null;
    }

    // Returns matched prefix length.
    public int matches(String otherUrl) {
        if (otherUrl.startsWith(url)) {
            return url.length();
        } else {
            return 0;
        }
    }

    // See: getRelativeUrl
    public int matchesRelative(String otherUrl) {
        if (otherUrl.startsWith(getRelativeUrl())) {
            return getRelativeUrl().length();
        } else {
            return 0;
        }
    }

    // Gets url relative to applications.
    // e.g. /app/home -> /home
    public String getRelativeUrl() {
        int secondSlash = url.indexOf("/", 1);

        if (secondSlash == -1)
            return "";

        return url.substring(secondSlash);
    }

    public HttpServletDelegator getServlet() {
        if (instance == null) {
            try {
                this.instance = new HttpServletDelegator(cls.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    public String getUrl() {
        return url;
    }
}