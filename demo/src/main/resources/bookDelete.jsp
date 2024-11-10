<html>
<head>
    <title>Delete Book</title>
</head>
<body>
<h1>Delete Book</h1>
<form action="/library/books/delete" method="post">
    <label for="id">Name:</label>
    <input type="text" id="id" name="id" required><br>
    <input type="submit" value="Delete Book">
</form>
</body>
</html>