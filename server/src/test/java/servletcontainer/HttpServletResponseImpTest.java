package servletcontainer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import servletcontainer.http.HttpServletResponseImp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class HttpServletResponseImpTest {

    private ByteArrayOutputStream clientStream;

    @Mock
    private Socket socketMock;

    private HttpServletResponseImp response;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        clientStream = new ByteArrayOutputStream();
        when(socketMock.getOutputStream()).thenReturn(clientStream);
        response = new HttpServletResponseImp(socketMock);
    }

    @AfterEach
    public void tearDown() {
        response.close();
    }

    @Test
    public void setGetStatus() {
        assertEquals(200, response.getStatus());
        response.setStatus(404);
        assertEquals(404, response.getStatus());
    }

    @Test
    public void setGetHeader() {
        assertEquals("text/html; charset=utf-8", response.getHeader("Content-Type"));
        response.setHeader("Content-Type", "text/plain");
        assertEquals("text/plain", response.getHeader("Content-Type"));
    }

    @Test
    public void getOutputStream() throws IOException {
        response.getWriter().write("Hello, World!");
        response.flushBuffer();

        String expected = "HTTP/1.1 200 Ok\n" +
                "Content-Type text/html; charset=utf-8\n" +
                "\n" +
                "Hello, World!";

        assertEquals(expected, clientStream.toString());
    }

    @Test
    public void flushBuffer() {
        response.flushBuffer();
        assertTrue(response.isBufferFlushed());
    }

    @Test
    public void reset() {
        response.setStatus(404);
        response.setHeader("Content-Type", "text/plain");
        response.reset();

        assertEquals(200, response.getStatus());
        assertEquals("text/html; charset=utf-8", response.getHeader("Content-Type"));
    }

    @Test
    public void setHeaderLock() {
        response.setHeader("Content-Type", "text/plain");
        response.setHeaderLock(true);

        response.setHeader("Content-Type", "text/html");
        assertEquals("text/plain", response.getHeader("Content-Type"));

        response.setHeaderLock(false);

        response.setHeader("Content-Type", "text/html");
        assertEquals("text/html", response.getHeader("Content-Type"));
    }

    @Test
    public void resetBuffer() throws IOException {
        response.getWriter().write("Hello, World!");
        response.resetBuffer();

        assertEquals("", clientStream.toString().trim());

        response.flushBuffer();
        String expected = "HTTP/1.1 200 Ok\n" +
                "Content-Type text/html; charset=utf-8\n\n";

        assertEquals(expected, clientStream.toString());
    }
}