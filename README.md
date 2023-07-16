# Servlet container

Servlet container działający podobnie jak Tomcat. Aplikacja musi być zapakowana do .war i przeniesiona do folderu `deploy` w  `server/src/main/resources`. Aplikacja będzie dostępna pod `localhost:8000/warName/`.

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
