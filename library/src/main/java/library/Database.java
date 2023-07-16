package library;

import library.orm.Book;

import java.util.ArrayList;
import java.util.List;

public class Database {
    private static int id = 0;
    private static List<Book> books;

    static {
        books = new ArrayList<>();
    }

    public List<Book> getAll() {
        return books;
    }

    public synchronized void add(String name, String author) {
        books.add(new Book(id++, name, author));
    }

    public synchronized void delete(int id) {
        int idx = -1;
        for(int i = 0; i < books.size(); i++)
            if (books.get(i).getId() == id) {
                idx = i;
                break;
            }

        if (idx != -1)
            books.remove(idx);
    }

    public synchronized Book getById(int id) {
        for(int i = 0; i < books.size(); i++)
            if (books.get(i).getId() == id) {
                return books.get(i);
            }
        return null;
    }

    public synchronized void update(int id, String name, String author) {
        for(int i = 0; i < books.size(); i++)
            if (books.get(i).getId() == id) {
                books.set(i, new Book(id, name, author));
            }
    }
}