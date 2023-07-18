package servletcontainer.servlet;

import servletcontainer.http.HttpServletRequestImp;
import servletcontainer.http.HttpServletResponseImp;
import servletcontainer.jsp.JSPTranspiler;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.ArrayList;
import java.util.List;

public class ServletContainer {
    // Directory with war files
    private String DEPLOY_URL = "src/main/resources/deploy";
    private final ExecutorService executor;
    private final ServletManager servletManager;
    private final JSPTranspiler jspTranspiler;

    private int port;
    private ServerSocket serverSocket;
    private boolean finish; // For graceful shutdown.

    public ServletContainer(int threads) {
        this.servletManager = new ServletManager();
        this.executor = Executors.newFixedThreadPool(threads);
        this.jspTranspiler = new JSPTranspiler();
    }

    public void start(int port) throws IOException {
        this.port = port;
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(1000);

        finish = false;
        while (!finish) {
            try {
                Socket clientSocket = serverSocket.accept();
                executor.execute(() -> {
                    try {
                        HttpServletRequestImp req = new HttpServletRequestImp(clientSocket, servletManager);
                        HttpServletResponseImp resp = (HttpServletResponseImp) req.createHttpServletResponse();
                        servletManager.getServlet(req.getUrl()).service(req, resp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (SocketTimeoutException ignored) {}
        }
    }

    public void stop() {
        finish = true;
    }

    public void addRoute(Class<? extends HttpServlet> cls, String path) {
        servletManager.addServlet(cls, path);
    }

    public void servletScan() {
        unzipWarFiles();

        compileJSP();
        loadClasses();

        removeUnzippedFiles();
    }

    public void servletScan(String url) {
        if (url != null)
            DEPLOY_URL = url;

        servletScan();
    }

    private void compileJSP() {
        File appFolder = new File(DEPLOY_URL);
        File[] apps = appFolder.listFiles();

        if (apps == null) return;

        for (File app : apps) {
            if (app.getName().endsWith(".war")) continue;

            compileJSPForWar(app, app, "");
        }
    }

    private void compileJSPForWar(File appDir, File dir, String servletUrl) {
        File[] files = dir.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().equals("WEB-INF")) continue;

                compileJSPForWar(appDir, file, servletUrl + "/" + file.getName());
                continue;
            }

            if (!file.getName().endsWith(".jsp")) continue;

            jspTranspiler.compile(appDir, file, servletUrl + "/" + file.getName());
        }

    }

    private void loadClasses() {
        File appFolder = new File(DEPLOY_URL);
        File[] apps = appFolder.listFiles();

        if (apps == null) return;

        for (File app : apps) {
            if (app.getName().endsWith(".war")) continue;

            List<URL> folderURLs = new ArrayList<>(getFolderURLs(app));
            List<String> classNames = new ArrayList<>(getClassNames(app, ""));

            URL[] urls = folderURLs.toArray(new URL[0]);
            ClassLoader cl = new URLClassLoader(urls);

            for (var clsName : classNames) {
                try {
                    Class<?> cls = cl.loadClass(clsName);
                    WebServlet annotation = cls.getAnnotation(WebServlet.class);

                    if (annotation != null) {
                        if (!HttpServlet.class.isAssignableFrom(cls)) continue;
                        servletManager.addServlet(
                                (Class<? extends HttpServlet>) cls,
                                "/" + app.getName() + annotation.value()[0]);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private List<URL> getFolderURLs(File folder) {
        List<URL> folderNames = new ArrayList<>();
        try {
            folderNames.add(folder.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        File[] files = folder.listFiles();

        if (files == null) return folderNames;

        for (File file : files)
            if (file.isDirectory()) folderNames.addAll(getFolderURLs(file));

        return folderNames;
    }

    private List<String> getClassNames(File folder, String packageName) {
        List<String> classNames = new ArrayList<>();

        File[] files = folder.listFiles();

        if (files == null) return classNames;

        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().startsWith("servletcontainer")) continue;
                if (file.getName().equals("WEB-INF") || file.getName().equals("classes"))
                    classNames.addAll(getClassNames(file, ""));
                else {
                    String newPackageName = packageName.isEmpty() ?
                            file.getName() : packageName + "." + file.getName();
                    classNames.addAll(getClassNames(file, newPackageName));
                }
            } else if (file.isFile() && file.getName().endsWith(".class")) {
                String className = file.getName().substring(0, file.getName().lastIndexOf("."));
                String newPackageName = packageName.isEmpty() ? className : packageName + "." + className;
                classNames.add(newPackageName);
            }
        }

        return classNames;
    }

    public void unzipWarFiles() {
        File folder = new File(DEPLOY_URL);
        File[] files = folder.listFiles();

        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".war")) {
                try {
                    String warFilePath = file.getAbsolutePath();
                    String warDirPath = warFilePath.substring(0, warFilePath.lastIndexOf("."));

                    File warDir = new File(warDirPath);
                    warDir.mkdir();

                    try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file))) {
                        ZipEntry entry = zipInputStream.getNextEntry();
                        while (entry != null) {
                            String entryName = entry.getName();
                            File entryFile = new File(warDir, entryName);
                            if (entry.isDirectory()) {
                                entryFile.mkdir();
                            } else {
                                try (FileOutputStream outputStream = new FileOutputStream(entryFile)) {
                                    byte[] buffer = new byte[1024];
                                    int length;
                                    while ((length = zipInputStream.read(buffer)) > 0) {
                                        outputStream.write(buffer, 0, length);
                                    }
                                }
                            }
                            entry = zipInputStream.getNextEntry();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void removeUnzippedFiles() {
        File folder = new File(DEPLOY_URL);
        File[] files = folder.listFiles();

        if (files == null) return;

        for (File file : files)
            if (!file.getName().endsWith(".war")) deleteFolder(file);
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();

        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                deleteFolder(f);
            } else {
                f.delete();
            }
        }
        folder.delete();
    }
}
