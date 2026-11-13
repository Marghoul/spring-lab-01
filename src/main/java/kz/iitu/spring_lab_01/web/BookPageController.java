package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.catalog.Book;
import kz.iitu.spring_lab_01.catalog.BookService;
import kz.iitu.spring_lab_01.catalog.Genre;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/books")
public class BookPageController {

    private final BookService service;

    public BookPageController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("books", service.findAll());
        model.addAttribute("genres", Genre.values());
        return "books/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Model model) {
        Book book = service.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("book", book);
        return "books/detail";
    }

    @PostMapping
    public String create(@RequestParam String title, @RequestParam Genre genre, @RequestParam int year) {
        service.create(title, genre, year);
        return "redirect:/books";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable long id) {
        service.delete(id);
        return "redirect:/books";
    }
}
