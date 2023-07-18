# Servlet container

![license](https://img.shields.io/github/license/leszkolukasz/servlet-container?colorA=192330&colorB=c70039&style=for-the-badge)
![status](https://img.shields.io/badge/status-finished-green?colorA=192330&colorB=00e600&style=for-the-badge)

This is a project that was created as part of a course I took at University of Warsaw. It is a simple implementation of a servlet container that supports Java servlets compliant with Java Servlet API. Even though it is quite barebone, it can load and run simple applications that use basic functionalities of servlets including dispatching, jsp and async servlets.

I did not find many other small implementations of servlet containers so feel free to use this as an example of how to create container like this. Keep in mind that due to time resctrictions it is a pretty imperfect, buggy project and while I tried to remain faithful to Servlet API I took some liberties here and there.

## Assumptions

The idea is that this container works in similar manner as tomcat. You can add servlet classes using ServletContainer::addRoute but preferred way it to use component scanning functionality. That is if application is packed into `warName.war` file and placed in `deploy` directory inside `server/src/main/resources` it will be loaded on server start and available under `localhost:8000/warName`.

By default server runs on port 8000. It can be changed in `Main` function. This function also contains example of how to start and configure this server.

## How to run

Project was build using Gradle. It contains two subprojects: server and demo application (simple book database). It was tested under Linux and I do not know if it even works under Windows/MacOS.

To run server use:

```
./gradlew server:run
```

## API

Starting server requires us to create instance of `ServletContainer`.

### `ServletContainer(int threads)`
Creates new isntance of `ServletContainer`. with given number of threads used to create ThreadPool. This pool will be used to handle http requests.

### `void ServletContainer::start(int port)`

Starts servlet container on given port. Keep in mind that .war files are not loaded at this point. They are only loaded when `ServletContainer:servletScan` is called.

### `void ServletContainer::stop()`

Gracefully stops container.

### `void ServletContainer::servletScan()`

Loads all .war files from `deploy` directory inside `server/src/main/resources`. It will load .jsp files as well and immediately transpile them into .class files. Only classes annotated with `@WebServlet` will be loaded. One caveat is that .war files I use have a specific directory structure that I am not so sure is the same as in most other .war files. You can check `war` gradle task in demo application to see what this structure should look like. This should be called at most once before start() method.

### `void ServletContainer::servletScan(String url)`

Same as `ServletContainer::servletScan()` but can loads .war files from given url.

### `void ServletContainer::addRoute(Class<? extends HttpServlet> cls, String path)`

Adds servlet to container. Servlet will only handle given url (see **URL resolution** paragraph for more details).

## Features

### Multithread support

The server supports concurrent handling of multiple clients at once. Each client will be handled by seperate thread from ThreadPool. Default pool size is 4 and can be changed in `Main` function.

### HttpServlet

The server will support any class that inherits from HttpServlet. Such a class can be added using `ServletContainer::addRoute`.

### HttpServletRequest and HttpServletResponse

The most important functionalities of these classes are implemented. You can write to the client using (only) `PrintWriter`, set headers, response status. HttpServletRequest can extract the request url, HTTP method (GET, POST, DELETE, PATCH), query parameters and parameters from the body (in the case of POST). Data is sent to the client after flushing the buffer or closing the `HttpServletResponse`. Using `RequestDispatcher`, you can send queries to other servlets including JSP sevlets.

### Async Servlet

There is support for async servlets. After `HttpServletRequest::startAsync` is executed, the request goes into asynchronous mode. There are two ways to use this mode. Any code will run as long as `AsyncContext::complete` is executed at some point, which terminates the connection to the client. You can also use `AsyncConext::start(Runnable)`, here you also need to execute `AsyncContext::complete` at some point. The latter is compatible with the Java Servlet API. It also supports timeout (`AsyncContext::setTimeout`) and `AsyncListener`. Async servlets are implemented using `CompletableFuture` so they will use ThreadPool seperate from the one used for synchronous clients.

### Component scanning

If the application has been zipped into .war and moved to the `deploy` folder, it will be loaded automatically. All classes annotated with `@WebServlet` and inheriting from `HttpServlet` will be added to the servlet container and will be available at `localhost:port/warName/servletUrl`. Each such class must have exactly one `servletUrl` specified in the `@WebServlet` annotation in the value attribute. If the application uses JSP it will be automatically compiled into .class and loaded.

### JSP

The server can transpile .jsp files to .class. There is support for almost all syntax in JSP. That includes:

- `<%@ page import/include=... %>`
- `<% ... %>`
- `<%! ... %>`
- `<%= ... %>`
- `<%-- %>`
- `${...}`

`${}` syntax supports Expression Language. Simple arithmetic operations can be performed, and any expression of the form `instance.property1.property2` will be converted to `request.getAttribute("instance").getProperty1().getProperty2()`. In `<% ... %>` there is also `out.println(...)` which writes directly to the client. JSP can be displayed using `RequestDispatcher::forward` or is available directly at `localhost:8000/warName/jspFileName.jsp`. A sample jsp action is available at `localhost:8000/library/jsp`. Keep in mind that I implemented parsing myself so weird code formatting/syntax may break it.

### FAQ
why do everything manually

## Aplikacja bilbioteczna

Do zaprezentowania działania serwera zaimplementowałem aplikacje biblioteczną. Frontend tworzony jest w pełni przy pomocy JSP. Aplikacja zapakowana jest do library.war i jest ładowana do serwera po jego uruchomieniu. Dostępne endpointy:

- **/library/books/show (GET)** - wypisuje wszystkie ksiażki
- **/library/books/add (GET/POST)** - dodaje nową książke
- **/library/books/update?id= (GET/POST)** - updatuje ksiązke o danym id
- **/library/books/delete (GET/POST)** - usuwa ksiązke

## Testy

Jest ponad 30 testów sprawdzających większość funkcjonalności i aplikację biblioteczną.
