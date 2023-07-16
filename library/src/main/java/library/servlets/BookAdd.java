package library.servlets;

import library.Database;

import javax.servlet.ServletException;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet(value = "/books/add")
public class BookAdd extends HttpServlet {
    private Database db;
    public BookAdd() {
        db = new Database();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        try {
            request.getRequestDispatcher("/bookAdd.jsp").forward(request, response);
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        String name = request.getParameter("name");
        String author = request.getParameter("author");

        db.add(name, author);
    }
}