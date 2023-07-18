package servletcontainer.servlet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServletWrapperTest {
    @Test
    void matches() {
        ServletWrapper wrapper = new ServletWrapper(null, "/test");
        assertEquals(5, wrapper.matches("/test"));
        assertEquals(5, wrapper.matches("/test/"));
        assertEquals(0, wrapper.matches("/tes"));
    }

    @Test
    void matchesRelative() {
        ServletWrapper wrapper = new ServletWrapper(null, "/app/test");
        assertEquals(5, wrapper.matchesRelative("/test"));
        assertEquals(5, wrapper.matchesRelative("/test/"));
        assertEquals(0, wrapper.matchesRelative("/tes"));
    }

    @Test
    void getRelativeUrl() {
        ServletWrapper wrapper = new ServletWrapper(null, "/app/");
        assertEquals(wrapper.getRelativeUrl(), "/");
        wrapper = new ServletWrapper(null, "/app/test");
        assertEquals(wrapper.getRelativeUrl(), "/test");
    }
}