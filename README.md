# Servlet container

![license](https://img.shields.io/github/license/leszkolukasz/servlet-container?colorA=192330&colorB=c70039&style=for-the-badge)
![status](https://img.shields.io/badge/status-finished-green?colorA=192330&colorB=00e600&style=for-the-badge)

This is a project that was created as part of a course I took at University of Warsaw. It is a simple implementation of a servlet container that supports Java servlets compliant with Java Servlet API. Even though it is quite barebone, it can load and run simple applications that use basic functionalities of servlets including dispatching, jsp and async servlets.

I did not find many other small implementations of servlet containers so feel free to use this as an example of how to create container like this. Keep in mind that due to time resctrictions it is a pretty imperfect, buggy project and while I tried to remain faithful to Servlet API I took some liberties here and there.

## Assumptions

The idea is that this container works in similar manner as tomcat. You can add servlet classes using ServletContainer::addRoute but preferred way it to use component scanning functionality. That is if application is packed into `warName.war` file and placed in `deploy` directory inside `server/src/main/resources` it will be loaded on server start and available under `localhost:8000/warName`.

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

### `ServletContainer::start(int port)`

Starts servlet container on given port. Keep in mind that .war files are not loaded at this point. They are only loaded when `ServletContainer:servletScan` is called.

### `ServletContainer::stop()`

Gracefully stops container.

### `ServletContainer::servletScan()`

Loads all .war files from `deploy` directory inside `server/src/main/resources`. It will load .jsp files as well and immediately transpile them into .class files. One caveat is that .war files I use have a specific directory structure that I am not so sure is the same as in most other .war files. You can check `war` gradle task in demo application to see what this structure should look like. This should be called at most once before start() method.

### `ServletContainer::servletScan(String url)`

Same as `ServletContainer::servletScan()` but can loads .war files from given url.

## Funkcjonalność

Domyślnie serwer startuje na porcie 8000. Można zmienić w funkcji Main. Serwer wspiera graceful shutdown po wykonaniu ServletContainer::stop. 

### Wielowątkowość

Serwer wspiera obsługę wielu klientów kilkoma wątkami. Domyślnie wykorzystywany jest ThreadPool rozmiaru 4. Rozmiar można zmienić w funkcji Main.

### HttpServlet

Serwer obłsugue dowolną klasę dziedziczącą po HttpServlet. Taką klasę można dodać korzystając z ServletManager::addServlet(cls, url).

### HttpServletRequest i HttpServletResponse

Najważniejsze funkcjonalności tych klas są zaimplementowane. Można pisać do klienta przy użyciu PrintWriter, ustawiać headery, status odpowiedzi. HttpServletRequest potrafi wyciągnąć url requesta, metodę HTTP (GET, POST, DELETE, PATCH), query parameters i parametry z body (w przypadku POSTa). Dane są wysyłane do klienta po zflushowaniu bufora lub zamknięciu HttpServletResponse. Za pomocą RequestDispatchera można wysyłać zapytania do innych servletów w tym sevletów JSP.

### Async Servlet

Jest wsparcie dla async servletów. Po wykonaniu HttpServletRequest::startAsync request przechodzi w tryb asynchroniczny. Są dwie możliwości korzystania z tego trybu. Dowolny kod będzie działał tak długo jak kiedyś wykona się AsyncContext::complete, które kończy połącznie z klientem (w tym można korzystać z async-http-client). Można również korzystać z AsyncConext::start(Runnable), tutaj też trzeba wykonać AsyncContext::complete. To drugie jest zgodne z Java API. Wspiera też timeout i AsyncListener.

### Component scanning

Jesli aplikacja została zapakowana do war i przeniesiona do folderu `deploy` będzie automatycznie załadowana. Wszystkie klasy z annotacją WebServlet i dziedziczące po HttpServlet będą dodane servlet containera i będą dostępne pod `localhost:port/warName/classUrl`. Każda taka klasa musi mieć co najmniej jeden `classUrl` wyspecyfikowany w annotacji WebServlet w atrybucie value. Jeśli aplikacja korzysta z JSP będzie ono automatycznie skopilowane do .class i załadowane.

### JSP

Serwer potrafi transpilować pliki .jsp do .class. Jest wparcie dla prawie całego syntaxa w JSP. Można korzystać z ```<%@ page import/include=... %>, <% ... %>, <%! ... %>, <%= ... %>, <%-- %>, ${...}```. ```${}``` pozwala na korzystanie z Expression Language. Można wykonywać proste operacje arytmetyczne, a także wszelkie wyrażenia postaci insatnce.property1.property2 będzie zamieniane na request.getAttribute("instance").getProperty1().getProperty2(). W ```<% ... %>``` działa też syntax ```out.println(...)```, który pisze bezposrednio do klienta. JSP można wyświetlać za pomocą RequestDispatcher::forward lub dostępne jest bezpośrednio pod adresem `localhost:8000/warName/jspFileName.jsp`. Przykładowe działanie jsp jest dostępne pod adresem `localhost:8000/library/hello`.

## Aplikacja bilbioteczna

Do zaprezentowania działania serwera zaimplementowałem aplikacje biblioteczną. Frontend tworzony jest w pełni przy pomocy JSP. Aplikacja zapakowana jest do library.war i jest ładowana do serwera po jego uruchomieniu. Dostępne endpointy:

- **/library/books/show (GET)** - wypisuje wszystkie ksiażki
- **/library/books/add (GET/POST)** - dodaje nową książke
- **/library/books/update?id= (GET/POST)** - updatuje ksiązke o danym id
- **/library/books/delete (GET/POST)** - usuwa ksiązke

## Testy

Jest ponad 30 testów sprawdzających większość funkcjonalności i aplikację biblioteczną.
