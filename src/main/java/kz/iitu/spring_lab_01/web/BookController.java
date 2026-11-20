package kz.iitu.spring_lab_01.web;

import jakarta.validation.Valid;
import kz.iitu.spring_lab_01.catalog.Book;
import kz.iitu.spring_lab_01.catalog.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/** REST API of the catalogue: GET /api/books, GET /api/books/{id}, POST /api/books. */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<Book> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Book one(@PathVariable long id) {
        return service.byId(id);   // BookNotFoundException -> 404 problem detail (ApiExceptionHandler)
    }

    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody CreateBookRequest request) {
        Book created = service.create(request.title(), request.genre(), request.year());
        return ResponseEntity.created(URI.create("/api/books/" + created.getId())).body(created);
    }
}
