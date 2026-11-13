package kz.iitu.spring_lab_01.catalog;

import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository books;

    public BookService(BookRepository books) {
        this.books = books;
    }

    @Transactional(readOnly = true)
    public List<Book> findAll() {
        return books.findAll(Sort.by("id"));
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(long id) {
        return books.findById(id);
    }

    /** The rule travels with the method: any caller, not only /books, needs ROLE_ADMIN. */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Book create(String title, Genre genre, int year) {
        return books.save(new Book(title, genre, year));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(long id) {
        books.deleteById(id);
    }
}
