package servletcontainer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncContextImp implements AsyncContext {
    final private List<AsyncListenerWrapper> listeners;
    private HttpServletRequestImp request;
    private HttpServletResponseImp response;
    private long timeout;

    public AsyncContextImp(HttpServletRequest request, HttpServletResponse response) {
        this.request = (HttpServletRequestImp) request;
        this.response = (HttpServletResponseImp) response;
        this.listeners = new ArrayList<>();
        this.timeout = 30000;
    }

    @Override
    public void addListener(AsyncListener listener) {
        listeners.add(new AsyncListenerWrapper(listener));
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest request,
                            ServletResponse response) {
        listeners.add(new AsyncListenerWrapper(listener,
                (HttpServletRequest) request,
                (HttpServletResponse) response));
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = (HttpServletRequestImp) request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = (HttpServletResponseImp) response;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return false;
    }

    @Override
    public void dispatch() {
    }

    @Override
    public void dispatch(String path) {
    }

    @Override
    public void dispatch(ServletContext context, String path) {
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void start(Runnable run) {
        if (timeout > 0) {
            CompletableFuture.runAsync(run)
                    .orTimeout(timeout, TimeUnit.MILLISECONDS)
                    .handle((result, throwable) -> {
                        if (throwable != null) {
                            if (throwable instanceof TimeoutException) {
                                listeners.forEach(AsyncListenerWrapper::notifyOnTimeout);

                                try {
                                    response.getWriter().println("ASYNC TIMEOUT");
                                    complete();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                listeners.forEach(listener -> listener.notifyOnError(throwable));
                            }
                        }
                        return null;
                    });
        } else {
            CompletableFuture.runAsync(run)
                    .handle((result, throwable) -> {
                        if (throwable != null)
                            listeners.forEach(listener -> listener.notifyOnError(throwable));
                        return null;
                    });
        }
    }

    public void notifyAndClearListenersOnStart() {
        listeners.forEach(AsyncListenerWrapper::notifyOnStartAsync);
        listeners.clear();
    }

    @Override
    public void complete() {
        listeners.forEach(AsyncListenerWrapper::notifyOnComplete);
        try {
            request.complete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class AsyncListenerWrapper {
        private final HttpServletRequest request;
        private final HttpServletResponse response;
        private final AsyncListener listener;

        public AsyncListenerWrapper(AsyncListener listener, HttpServletRequest request,
                                    HttpServletResponse response) {
            this.listener = listener;
            this.request = request;
            this.response = response;
        }

        public AsyncListenerWrapper(AsyncListener listener) {
            this(listener, null, null);
        }

        public void notifyOnStartAsync() {
            try {
                listener.onStartAsync(new AsyncEventImp(AsyncContextImp.this, request, response));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void notifyOnComplete() {
            try {
                listener.onComplete(new AsyncEventImp(AsyncContextImp.this, request, response));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void notifyOnTimeout() {
            try {
                listener.onTimeout(new AsyncEventImp(AsyncContextImp.this, request, response));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void notifyOnError(Throwable err) {
            try {
                listener.onError(new AsyncEventImp(AsyncContextImp.this, request, response, err));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
