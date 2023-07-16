package servletcontainer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServlet;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
class JSPTranspilerTest {
    private ByteArrayOutputStream clientStream;

    @Mock
    private Socket socketMock;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        clientStream = new ByteArrayOutputStream();
        when(socketMock.getOutputStream()).thenReturn(clientStream);
    }

    @Test
    void transpile() throws IOException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        File appDir = new File("src/test/resources/test");
        File jspFile = new File("src/test/resources/test/test.jsp");

        JSPTranspiler transpiler = new JSPTranspiler();
        transpiler.compile(appDir, jspFile, "/test/test.jsp");

        URL[] urls = {appDir.toURI().toURL()};
        ClassLoader cl = new URLClassLoader(urls);

        Class<?> servletCls = cl.loadClass("test");
        HttpServletDelegator servlet = new HttpServletDelegator(
                (HttpServlet) servletCls.getDeclaredConstructor().newInstance());

        HttpServletRequestImp request = mock(HttpServletRequestImp.class);
        when(request.getAttribute("year")).thenReturn(2023);
        HttpServletResponseImp response = new HttpServletResponseImp(socketMock);

        servlet.doGet(request, response);
        response.close();

        String expected = """
             HTTP/1.1 200 Ok
             Content-Type text/html; charset=utf-8
                      
                      
                      
                      
             <!DOCTYPE html>
             <html>
             <body>
             <h1>Expression lanugage</h1>
             Hello world 2023
             <br>
             Hello world 2023
             <br>
             2137
             <br>
                      
             <h1>Scriplet tag</h1>
                      
             10
             3
             <br>
                      
             <h1>Declaration tag + Expression tag</h1>
                      
             42
             <br>
             4242
             <br>
                      
             <h1> Mixed </h1>
             -1 <br>
             0 <br>
             1 <br>
             2 <br>
             3 <br>
             </body>
             </html>
             </html>     
             """;


        assertEquals(expected.strip(), clientStream.toString().strip());
    }
}