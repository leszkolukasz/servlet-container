package library.servlets;

import library.Database;
import library.orm.Book;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.*;
import javax.servlet.http.*;

@WebServlet(value = "/books/show")
public class BookShow extends HttpServlet {
    private Database db;
    public BookShow() {
        db = new Database();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        List<Book> books = db.getAll();

        request.setAttribute("books", books);
        try {
            request.getRequestDispatcher("/bookShow.jsp").forward(request, response);
        } catch (ServletException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}