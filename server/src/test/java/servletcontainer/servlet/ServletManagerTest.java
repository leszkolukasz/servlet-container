package servletcontainer.servlet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import servletcontainer.utils.EmptyServlet;

import static org.junit.jupiter.api.Assertions.*;

class ServletManagerTest {

    private ServletManager servletManager;

    @BeforeEach
    void setUp() {
        servletManager = new ServletManager();
    }

    @Test
    void addServlet() {
        assertNull(servletManager.getServletWrapper(""));
        servletManager.addServlet(null, "/test");
        assertNotNull(servletManager.getServletWrapper("/test"));
    }

    @Test
    void getServlet() {
        servletManager.addServlet(null, "/test");
        assertThrows(Exception.class, () -> {servletManager.getServlet("/test");});

        servletManager.addServlet(EmptyServlet.class, "/test");
        var servlet = servletManager.getServlet("/test");

        // Singleton
        assertSame(servlet, servletManager.getServlet("/test"));
    }

    @Test
    void getServletWrapper() {
        assertNull(servletManager.getServletWrapper(""));

        servletManager.addServlet(null, "/test");
        servletManager.addServlet(null, "/test/more");

        assertEquals(servletManager.getServletWrapper("/test/").getUrl(), "/test");
        assertEquals(servletManager.getServletWrapper("/test/more").getUrl(), "/test/more");
    }

    @Test
    void getServletWrapperWithRelativeURL() {
        assertNull(servletManager.getServletWrapperWithRelativeURL(""));
        servletManager.addServlet(null, "/app/test");
        assertNotNull(servletManager.getServletWrapperWithRelativeURL("/test"));
    }
}