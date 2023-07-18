package servletcontainer.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import servletcontainer.servlet.ServletManager;

import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
class HttpServletRequestImpTest {
    @Mock
    private Socket socketMock;
    @Mock
    private ServletManager servletManagerMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getMethod() throws IOException {
        String request = "GET /path HTTP/1.1\n" +
                "Host: localhost:8080\n\n";

        var in =  new ByteArrayInputStream(request.getBytes());
        when(socketMock.getInputStream()).thenReturn(in);

        HttpServletRequestImp httpServletRequest = new HttpServletRequestImp(socketMock, null);

        assertEquals("GET", httpServletRequest.getMethod());
    }

    @Test
    public void getUrl() throws IOException {
        String request = "GET /path HTTP/1.1\n" +
                "Host: localhost:8080\n\n";

        var in =  new ByteArrayInputStream(request.getBytes());
        when(socketMock.getInputStream()).thenReturn(in);

        HttpServletRequestImp httpServletRequest = new HttpServletRequestImp(socketMock, null);

        assertEquals("/path/", httpServletRequest.getUrl());
    }

    @Test
    public void getQueryParameter() throws IOException {
        String request = "GET /path?name=value HTTP/1.1\n" +
                "Host: localhost:8080\n\n";

        var in =  new ByteArrayInputStream(request.getBytes());
        when(socketMock.getInputStream()).thenReturn(in);

        HttpServletRequestImp httpServletRequest = new HttpServletRequestImp(socketMock, null);

        assertEquals("value", httpServletRequest.getParameter("name"));
    }

    @Test
    public void getParameter() throws IOException {
        String request = "POST /path HTTP/1.1\n" +
                "Host: localhost:8080\n\n" +
                "name=value&age=30";

        var in =  new ByteArrayInputStream(request.getBytes());
        when(socketMock.getInputStream()).thenReturn(in);

        HttpServletRequestImp httpServletRequest = new HttpServletRequestImp(socketMock, servletManagerMock);

        assertEquals("value", httpServletRequest.getParameter("name"));
        assertEquals("30", httpServletRequest.getParameter("age"));
    }
}