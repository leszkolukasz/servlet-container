<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Nice</title>
</head>
<body>
<h1>Expression lanugage</h1>
Hello world ${year}
<br>
${"Hello world " + year}
<br>
${2023 + (114 - 2) / 4 * 2 + 58}
<br>

<h1>Scriplet tag</h1>

<% List<Integer> l = new ArrayList<>();
    l.add(10);
    l.add(20);
    l.add(30);
    out.println(l.get(0));
    out.println(l.size());
%>
<br>

<h1>Declaration tag + Expression tag</h1>
<%! int x = 42;%>
<%= x %>
<br>
<%= x * 100 + 42 %>
<br>

<h1> Mixed </h1>
<% for (int i = 0; i < 5; i++) {%>
<%= i - 1 %> <br>
<% } %>
</body>
</html>