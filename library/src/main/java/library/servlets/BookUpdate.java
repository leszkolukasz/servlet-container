package library.servlets;

import library.Database;
import library.orm.Book;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

@WebServlet(value = "/books/update")
public class BookUpdate extends HttpServlet {
    private final Database db;

    public BookUpdate() {
        db = new Database();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        int id = Integer.parseInt(request.getParameter("id"));
        Book book = db.getById(id);
        request.setAttribute("book", book);

        try {
            request.getRequestDispatcher("/bookUpdate.jsp").forward(request, response);
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        String author = request.getParameter("author");

        db.update(id, name, author);
    }
}