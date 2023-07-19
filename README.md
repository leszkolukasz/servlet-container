# Servlet container

![license](https://img.shields.io/github/license/leszkolukasz/servlet-container?colorA=192330&colorB=c70039&style=for-the-badge)
![status](https://img.shields.io/badge/status-finished-green?colorA=192330&colorB=00e600&style=for-the-badge)

[![Open Source Helpers](https://www.codetriage.com/leszkolukasz/servlet-container/badges/users.svg)](https://www.codetriage.com/leszkolukasz/servlet-container)

This is a project that was created as part of a course I took at the University of Warsaw. It is a simple implementation of a servlet container that supports Java servlets compliant with Java Servlet API. Even though it is quite barebone, it can load and run simple applications that use basic functionalities of servlets including dispatching, JSP and async servlets.

I did not find many other small implementations of servlet containers so feel free to use this as an example of how to create a container like this. Keep in mind that due to time restrictions it is a pretty imperfect, buggy project and while I tried to remain faithful to Servlet API I took some liberties here and there.

## Assumptions

The idea is that this container works in a similar manner as Tomcat. You can add servlet classes using ServletContainer::addRoute but the preferred way is to use component scanning functionality. That is if the application is zipped into `warName.war` file and placed in `deploy` directory inside `server/src/main/resources` it will be loaded on the server start and available under `localhost:8000/warName`.

By default, the server runs on port 8000. It can be changed in `Main` function. This function also contains and example of how to start and configure this server.

## How to run

The project was built using Gradle. It contains two subprojects: server and demo application (simple book database). It was tested under Linux and I do not know if it even works under Windows/MacOS.

To run server use:

```
./gradlew server:run
```

## API

Starting server requires us to create an instance of `ServletContainer`.

### `ServletContainer(int threads)`
Creates a new instance of `ServletContainer`. with given number of threads used to create ThreadPool. This pool will be used to handle HTTP requests.

### `void ServletContainer::start(int port)`

Starts servlet container on given port. Keep in mind that .war files are not loaded at this point. They are only loaded when `ServletContainer:servletScan` is called.

### `void ServletContainer::stop()`

Gracefully stops the container.

### `void ServletContainer::servletScan()`

Loads all .war files from `deploy` directory inside `server/src/main/resources`. It will load .jsp files as well and immediately transpile them into .class files. Only classes annotated with `@WebServlet` will be loaded. One caveat is that .war files I use have a specific directory structure that I am not so sure is the same as in most other .war files. You can check `war` gradle task in demo application to see what this structure should look like. This method should be called at most once before start() method.

### `void ServletContainer::servletScan(String url)`

Same as `ServletContainer::servletScan()` but can load .war files from given url.

### `void ServletContainer::addRoute(Class<? extends HttpServlet> cls, String path)`

Adds servlet to the container. Servlet will only handle given url (see **FAQ** for more details).

## Features

### Multithread support

The server supports concurrent handling of multiple clients at once. Each client will be handled by separate thread from ThreadPool. The default pool size is 4 and can be changed in `Main` function.

### HttpServlet

The server will support any class that inherits from HttpServlet. Such a class can be added using `ServletContainer::addRoute`.

### HttpServletRequest and HttpServletResponse

The most important functionalities of these classes are implemented. You can write to the client using (only) `PrintWriter`, set headers and response status. HttpServletRequest can extract the request URL, HTTP method (GET, POST, DELETE, PATCH), query parameters and parameters from the body (in the case of POST). Data is sent to the client after flushing the buffer or closing the `HttpServletResponse`. Using `RequestDispatcher`, you can send queries to other servlets including JSP servlets.

### Async Servlet

There is support for async servlets. After `HttpServletRequest::startAsync` is executed, the request goes into asynchronous mode. There are two ways to use this mode. Any code will run as long as `AsyncContext::complete` is executed at some point, which terminates the connection to the client. You can also use `AsyncConext::start(Runnable)`, here you also need to execute `AsyncContext::complete` at some point. The latter is compatible with the Java Servlet API. It also supports timeout (`AsyncContext::setTimeout`) and `AsyncListener`. Async servlets are implemented using `CompletableFuture` so they will use ThreadPool separate from the one used for synchronous clients.

### Component scanning

If the application has been zipped into .war and moved to the `deploy` folder, it will be loaded automatically. All classes annotated with `@WebServlet` and inheriting from `HttpServlet` will be added to the servlet container and will be available at `localhost:port/warName/servletUrl`. Each such class must have exactly one `servletUrl` specified in the `@WebServlet` annotation in the value attribute. If the application uses JSP it will be automatically compiled into .class and loaded. Many applications can be loaded, however, I do not know what happens when there are the same class names used in two applications.

### JSP

The server can transpile .jsp files to .class. There is support for almost all syntax in JSP. That includes:

- `<%@ page import/include=... %>`
- `<% ... %>`
- `<%! ... %>`
- `<%= ... %>`
- `<%-- %>`
- `${...}`

`${}` syntax partially supports Expression Language. Simple arithmetic operations can be performed, and any expression of the form `instance.property1.property2` will be converted to `request.getAttribute("instance").getProperty1().getProperty2()`. In `<% ... %>` there is also `out.println(...)` which writes directly to the client. JSP can be displayed using `RequestDispatcher::forward` or is available directly at `localhost:8000/warName/jspFileName.jsp`. A sample jsp action is available at `localhost:8000/library/jsp`. Keep in mind that I implemented parsing myself so weird code formatting/syntax may break it.

## Demo application

To demonstrate servlet container, I implemented a simple application that simulates a book database. Books can be added, removed and updated using HTML forms. All book can also be viewed in HTML table. The frontend is fully developed using JSP. The application is zipped into library.war using `war` gradle task, moved to `deploy` and loaded to the server when it starts.

Endpoints:

- **/library/books/show (GET)** - lists all books
- **/library/books/add (GET/POST)** - adds new book
- **/library/books/update?id= (GET/POST)** - updates book with given id
- **/library/books/delete (GET/POST)** - removed book

## Tests

There are over 30 tests to check most of the functionality and the demo application. It is preferred to run them from Intellij due to problems explained in **FAQ**. Alternatively, they can be run using:
```
./gradlew test
```

## FAQ

### 1) Why all the parsing is done manually?
   
This project was part of a university course and I was not allowed to use any 3rd party libraries except for Junit.

### 2) Why do some tests fail?

There are problems with socket ports being already in use. I never got down to fix it but most of the time it works when run from Intellij.

### 3) Why it is possible for a servlet to handle url that it should not handle?

Right now the resolution of which servlet should handle the given url is quite primitive. Basically given `url` server looks for a servlet whose `servletUrl` is a prefix of `url`. If there are many the one with the longest prefix is selected. This means that if a servlet has `servletUrl` equal to `/app/home` it will also handle `/app/home/nonexisting/`, `/app/home/12345` etc.

## Contribution

Anyone is welcome to contribute to this project. I created a few good-first-issue issues, so feel free to check them out :)
