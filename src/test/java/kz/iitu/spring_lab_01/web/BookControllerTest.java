package kz.iitu.spring_lab_01.web;

import kz.iitu.spring_lab_01.catalog.Book;
import kz.iitu.spring_lab_01.catalog.BookNotFoundException;
import kz.iitu.spring_lab_01.catalog.BookService;
import kz.iitu.spring_lab_01.catalog.Genre;
import kz.iitu.spring_lab_01.security.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Task 2: web slice. Only BookController, ApiExceptionHandler and the MVC/security
 * infrastructure are loaded; BookService is a @MockitoBean.
 * The REAL SecurityConfiguration is imported, and the tests run as a mock user
 * (@WithMockUser) - security is exercised, not switched off.
 */
@WebMvcTest(BookController.class)
@Import(SecurityConfiguration.class)
class BookControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean BookService service;

    private static Book book(long id, String title) {
        Book b = new Book(title, Genre.FANTASY, 1965);
        ReflectionTestUtils.setField(b, "id", id);
        return b;
    }

    @Test
    @WithMockUser(roles = "USER")
    void existingBookIsReturnedAsJson() throws Exception {
        given(service.byId(1L)).willReturn(book(1L, "Dune"));

        mockMvc.perform(get("/api/books/1"))
               .andExpect(status().isOk())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(1))
               .andExpect(jsonPath("$.title").value("Dune"))
               .andExpect(jsonPath("$.genre").value("FANTASY"))
               .andExpect(jsonPath("$.year").value(1965));
    }

    @Test
    @WithMockUser(roles = "USER")
    void missingBookBecomes404WithAProblemDetail() throws Exception {
        given(service.byId(7L)).willThrow(new BookNotFoundException(7L));

        mockMvc.perform(get("/api/books/7"))
               .andExpect(status().isNotFound())
               .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
               .andExpect(jsonPath("$.status").value(404))
               .andExpect(jsonPath("$.title").value("Book not found"))
               .andExpect(jsonPath("$.detail").value("Book 7 not found"))
               .andExpect(jsonPath("$.bookId").value(7));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidBodyIsRejectedWith400BeforeTheServiceIsCalled() throws Exception {
        mockMvc.perform(post("/api/books")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content("""
                               {"title": "", "genre": null, "year": 1200}
                               """))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.status").value(400))
               .andExpect(jsonPath("$.detail").exists());
        verifyNoInteractions(service);
    }

    @Test
    void anonymousCallerGets401BecauseSecurityIsActiveInTheSlice() throws Exception {
        mockMvc.perform(get("/api/books/1")).andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "USER")
    void ordinaryUserCannotCreateABook() throws Exception {
        mockMvc.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON)
                       .content("{\"title\":\"Dune\",\"genre\":\"FANTASY\",\"year\":1965}"))
               .andExpect(status().isForbidden());
        verifyNoInteractions(service);
    }
}
