package servletcontainer.http;

import servletcontainer.servlet.ServletWrapper;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class RequestDispatcherImp implements RequestDispatcher {
    final private ServletWrapper servletWrapper;

    public RequestDispatcherImp(ServletWrapper servletWrapper) {
        this.servletWrapper = servletWrapper;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) {
        var resp = (HttpServletResponseImp) response;
        var req = (HttpServletRequestImp) request;

        if (resp.isBufferFlushed())
            throw new IllegalStateException();

        req.setUrl(servletWrapper.getUrl());
        resp.resetBuffer();
        this.servletWrapper.getServlet().service(req, resp);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) {
        var resp = (HttpServletResponseImp) response;
        var req = (HttpServletRequestImp) request;

        resp.setHeaderLock(true);
        servletWrapper.getServlet().dispatchRequest(req, resp);
        resp.setHeaderLock(false);
    }
}
