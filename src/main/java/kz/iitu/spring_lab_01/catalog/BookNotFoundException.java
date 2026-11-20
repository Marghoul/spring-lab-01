package kz.iitu.spring_lab_01.catalog;

public class BookNotFoundException extends RuntimeException {

    private final long id;

    public BookNotFoundException(long id) {
        super("Book " + id + " not found");
        this.id = id;
    }

    public long getId() { return id; }
}
