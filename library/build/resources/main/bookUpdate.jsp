<%@ page import="library.orm.Book" %>
<html>
<head>
  <title>Update Book</title>
</head>
<body>
<h1>Update Book</h1>
<form action="/library/books/update" method="post">
  <input type="hidden" name="id" value="${ book.id }">
  <label for="name">Name:</label>
  <input type="text" id="name" name="name" value="${ book.name }"><br>
  <label for="author">Author:</label>
  <input type="text" id="author" name="author" value="${ book.author }"><br>
  <input type="submit" value="Update">
</form>
</body>
</html>