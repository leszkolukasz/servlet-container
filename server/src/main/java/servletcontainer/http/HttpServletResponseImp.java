package servletcontainer.http;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpServletResponseImp implements HttpServletResponse {
    final private Socket client;

    // Accumulates data. Data is sent to client only when buffer is flushed
    // or HttpServletResponse is closed.
    private final StringWriter buffer;
    private final PrintWriter bufferWriter;
    private final PrintWriter clientOutputStream;
    private final Map<String, String> headers;

    private HttpStatus httpStatus = HttpStatus.OK; // Default http status is 200.
    private boolean bufferFlushed = false;

    // This is only needed when RequestDispatcher is called with method `include`.
    // Included servlets cannot change headers.
    private boolean headerLocked = false;

    public HttpServletResponseImp(Socket client) throws IOException {
        this.client = client;
        this.buffer = new StringWriter();
        this.bufferWriter = new PrintWriter(this.buffer, true);
        this.clientOutputStream = new PrintWriter(client.getOutputStream(), true);
        this.headers = new HashMap<>();

        // By default, response is of type text/html.
        this.headers.put("Content-Type", "text/html; charset=utf-8");
    }

    @Override
    public int getStatus() {
        return httpStatus.getValue();
    }

    @Override
    public void setStatus(int code) {
        if (headerLocked)
            return;

        this.httpStatus = HttpStatus.findByCode(code);
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override// Accumulates data. Data
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

    }

    @Override
    public void sendError(int sc) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {
        if (headerLocked)
            return;
        headers.put(name, value);
    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public void setStatus(int sc, String sm) {

    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public void setContentType(String type) {

    }

    // Only getWriter is supported.
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        return bufferWriter;
    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long length) {

    }

    @Override
    public int getBufferSize() {
        return buffer.getBuffer().length();
    }

    @Override
    public void setBufferSize(int size) {
        buffer.getBuffer().setLength(size);
    }

    private void flushHeaders() {
        // Headers can only be flushed before body is flushed.
        if (bufferFlushed)
            return;

        clientOutputStream.println("HTTP/1.1 " + httpStatus);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            clientOutputStream.println(entry.getKey() + " " + entry.getValue());
        }

        clientOutputStream.println();
    }

    @Override
    public void flushBuffer() {
        flushHeaders();
        clientOutputStream.write(buffer.getBuffer().toString());
        clientOutputStream.flush();
        resetBuffer();
        bufferFlushed = true;
    }

    public boolean isBufferFlushed() {
        return bufferFlushed;
    }

    @Override
    public void reset() {
        if (bufferFlushed)
            throw new IllegalStateException();

        httpStatus = HttpStatus.OK;
        headers.clear();
        headers.put("Content-Type", "text/html; charset=utf-8");
        resetBuffer();
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public void setLocale(Locale loc) {

    }

    public void setHeaderLock(boolean locked) {
        headerLocked = locked;
    }

    @Override
    public void resetBuffer() {
        buffer.getBuffer().setLength(0);
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    public void close() {
        flushBuffer();
        bufferWriter.close();
        clientOutputStream.close();
    }
}
