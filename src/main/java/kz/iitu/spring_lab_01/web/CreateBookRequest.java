package kz.iitu.spring_lab_01.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.iitu.spring_lab_01.catalog.Genre;

/** Request body of POST /api/books, checked by Bean Validation (@Valid). */
public record CreateBookRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull Genre genre,
        @Min(1450) @Max(2100) int year) {
}
