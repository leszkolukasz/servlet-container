package servletcontainer;

import servletcontainer.servlet.ServletContainer;

import java.io.IOException;

public class Main {
    public static void main(String... args) throws IOException {
        ServletContainer servletContainer = new ServletContainer(4);
        servletContainer.servletScan();
        servletContainer.start(8000);
    }
}
