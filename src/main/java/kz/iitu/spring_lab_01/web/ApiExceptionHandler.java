package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.catalog.BookNotFoundException;
import kz.iitu.spring_lab_01.report.InvalidReportPeriodException;
import kz.iitu.spring_lab_01.report.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

/**
 * Turns domain exceptions of the REST API into RFC 9457 problem details.
 * Extending ResponseEntityExceptionHandler also turns a failed @Valid body
 * (MethodArgumentNotValidException) into a 400 problem detail.
 */
@RestControllerAdvice(assignableTypes = {BookController.class, ReportController.class})
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BookNotFoundException.class)
    ProblemDetail bookNotFound(BookNotFoundException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("Book not found");
        pd.setType(URI.create("https://spring-lab-01/problems/book-not-found"));
        pd.setProperty("bookId", e.getId());
        return pd;
    }

    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail userNotFound(UserNotFoundException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("User not found");
        return pd;
    }

    @ExceptionHandler(InvalidReportPeriodException.class)
    ProblemDetail invalidPeriod(InvalidReportPeriodException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Invalid report period");
        return pd;
    }
}
