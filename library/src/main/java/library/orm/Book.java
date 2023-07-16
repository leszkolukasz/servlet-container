package library.orm;

public class Book {
    private int id;
    private String name;
    private String author;

    public Book(int id, String name, String author)
    {
        this.id = id;
        this.name = name;
        this.author = author;
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }
}