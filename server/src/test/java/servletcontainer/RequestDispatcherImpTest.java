package servletcontainer;

import org.junit.jupiter.api.Test;
import servletcontainer.http.HttpServletDelegator;
import servletcontainer.http.HttpServletRequestImp;
import servletcontainer.http.HttpServletResponseImp;
import servletcontainer.http.RequestDispatcherImp;
import servletcontainer.servlet.ServletWrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RequestDispatcherImpTest {

    @Test
    void forward() {
        HttpServletResponseImp responseMock = mock(HttpServletResponseImp.class);
        when(responseMock.isBufferFlushed()).thenReturn(true);

        var dispatcher = new RequestDispatcherImp(null);
        assertThrows(IllegalStateException.class, () -> {dispatcher.forward(null, responseMock);});

        when(responseMock.isBufferFlushed()).thenReturn(false);
        ServletWrapper wrapperMock = mock(ServletWrapper.class);
        HttpServletDelegator servletMock = mock(HttpServletDelegator.class);
        HttpServletRequestImp requestMock = mock(HttpServletRequestImp.class);
        when(wrapperMock.getServlet()).thenReturn(servletMock);

        var dispatcher2 = new RequestDispatcherImp(wrapperMock);
        dispatcher2.forward(requestMock, responseMock);
        verify(servletMock).service(requestMock, responseMock);
    }

    @Test
    void include() {
        HttpServletResponseImp responseMock = mock(HttpServletResponseImp.class);
        ServletWrapper wrapperMock = mock(ServletWrapper.class);
        HttpServletDelegator servletMock = mock(HttpServletDelegator.class);
        HttpServletRequestImp requestMock = mock(HttpServletRequestImp.class);
        when(wrapperMock.getServlet()).thenReturn(servletMock);

        var dispatcher = new RequestDispatcherImp(wrapperMock);
        dispatcher.include(requestMock, responseMock);
        verify(responseMock, times(2)).setHeaderLock(any(Boolean.class));
        verify(servletMock).dispatchRequest(requestMock, responseMock);
    }
}