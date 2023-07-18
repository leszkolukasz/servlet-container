package servletcontainer.servlet;

import servletcontainer.http.HttpServletDelegator;
import servletcontainer.routes.DefaultServlet;
import servletcontainer.routes.TimeoutServlet;

import javax.servlet.http.HttpServlet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ServletManager {
    final private List<ServletWrapper> servlets;

    public ServletManager() {
        this.servlets = new ArrayList<>();
        addServlet(DefaultServlet.class, "/");
        addServlet(TimeoutServlet.class, "/timeout");
    }

    public synchronized void addServlet(Class<? extends HttpServlet> cls, String url) {
        // Remove servlet for `url` if there is one already.
        for (int i = 0; i < servlets.size(); i++) {
            if (servlets.get(i).getUrl().equals(url)) {
                servlets.remove(i);
                break;
            }
        }

        servlets.add(new ServletWrapper(cls, url));
    }

    // Finds servlet that is best match for `url`.
    public synchronized HttpServletDelegator getServlet(String url) {
        var wrapper = getServletWrapper(url);
        return wrapper == null ? null : wrapper.getServlet();
    }

    // Finds servlet wrapper that is best match for `url`.
    public synchronized ServletWrapper getServletWrapper(String url) {
        ServletWrapper bestMatch = servlets.stream()
                .max(Comparator.comparingInt(wrapper -> wrapper.matches(url)))
                .orElse(null);

        if (bestMatch == null) return null;

        // bestMatch is nonnull as long as there is at least one servlet. But this servlet may not match `url`.
        if (bestMatch.matches(url) == 0) return null;

        return bestMatch;
    }

    // See: ServletWrapper::matchesRelative
    public synchronized ServletWrapper getServletWrapperWithRelativeURL(String url) {
        ServletWrapper bestMatch = servlets.stream()
                .max(Comparator.comparingInt(wrapper -> wrapper.matchesRelative(url)))
                .orElse(null);

        if (bestMatch == null) return null;

        if (bestMatch.matchesRelative(url) == 0) return null;

        return bestMatch;
    }
}
