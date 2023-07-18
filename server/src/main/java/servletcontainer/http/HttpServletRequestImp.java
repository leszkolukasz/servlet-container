package servletcontainer.http;

import servletcontainer.servlet.ServletManager;
import servletcontainer.async.AsyncContextImp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class HttpServletRequestImp implements HttpServletRequest {
    private final Socket client;
    private final ServletManager servletManager;
    private final Map<String, String> headers;
    private final Map<String, Object> attributes; // Used for data transfer between servlets.
    private final Map<String, String> parameters; // In POST these are body parameters.
    private final Map<String, String> queryParameters;

    private HttpServletResponseImp httpServletResponse;
    private AsyncContextImp asyncContext;
    private String url;
    private String httpMethod;

    private boolean isAsync = false;

    public HttpServletRequestImp(Socket client, ServletManager servletManager) {
        this.client = client;
        this.servletManager = servletManager;

        attributes = new HashMap<>();
        headers = new HashMap<>();
        parameters = new HashMap<>();
        queryParameters = new HashMap<>();

        parseHttpRequest();
    }

    private void parseHttpRequest() {
        try {
            var in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            String line = in.readLine();
            httpMethod = line.split(" ")[0];
            setUrl(line.split(" ")[1]);

            int questionMarkIndex = url.indexOf('?');
            if (questionMarkIndex != -1) {
                String query = url.substring(questionMarkIndex + 1);
                query = query.substring(0, query.length() - 1);
                String[] queryParamsArray = query.split("&");
                for (String queryParam : queryParamsArray) {
                    String[] keyValue = queryParam.split("=");
                    String key = keyValue[0];
                    String value = keyValue.length > 1 ? keyValue[1] : "";
                    queryParameters.put(key, value);
                }
            }

            while ((line = in.readLine()).length() != 0) {
                headers.put(line.split(": ")[0], line.split(": ")[1]);
            }

            StringBuilder payload = new StringBuilder();
            int contentLength = headers.get("Content-Length") != null ?
                    Integer.parseInt(headers.get("Content-Length")) : 0;

            for (int i = 0; i < contentLength || in.ready(); i++)
                payload.append((char) in.read());

            String payloadStr = payload.toString();
            if (payloadStr.equals(""))
                return;

            String[] pairs = payloadStr.split("&");

            for (String pair : pairs)
                parameters.put(pair.split("=")[0], pair.split("=")[1]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpServletResponse createHttpServletResponse() {
        if (httpServletResponse != null)
            return httpServletResponse;

        try {
            httpServletResponse = new HttpServletResponseImp(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return httpServletResponse;
    }

    @Override
    public String getMethod() {
        return httpMethod;
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response)
            throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> httpUpgradeHandlerClass)
            throws IOException, ServletException {
        return null;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String otherUrl) {
        url = otherUrl;
        if (!url.endsWith("/"))
            url = url + "/";
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }


    @Override
    public String getParameter(String name) {
        var value = parameters.get(name);
        if (value != null)
            return value;

        return queryParameters.get(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        var names = parameters.keySet();
        names.addAll(queryParameters.keySet());

        return Collections.enumeration(names);
    }

    @Override
    public String[] getParameterValues(String name) {
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String url) {
        return new RequestDispatcherImp(servletManager.getServletWrapperWithRelativeURL(url));
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public boolean isAsyncStarted() {
        return isAsync;
    }

    @Override
    public boolean isAsyncSupported() {
        return true;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return asyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }

    @Override
    public AsyncContext startAsync() {
        if (asyncContext != null) {
            asyncContext.notifyAndClearListenersOnStart();
            return asyncContext;
        }

        isAsync = true;
        asyncContext =  new AsyncContextImp(this, httpServletResponse);
        return asyncContext;
    }

    @Override
    public AsyncContext startAsync(ServletRequest request, ServletResponse response) {
        if (asyncContext != null) {
            asyncContext.notifyAndClearListenersOnStart();
            asyncContext.setRequest((HttpServletRequest) request);
            asyncContext.setResponse((HttpServletResponse) response);
            return asyncContext;
        }

        isAsync = true;
        asyncContext = new AsyncContextImp(
                (HttpServletRequest) request,
                (HttpServletResponse) response
        );
        return asyncContext;
    }

    public void complete() {
        httpServletResponse.close();
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
