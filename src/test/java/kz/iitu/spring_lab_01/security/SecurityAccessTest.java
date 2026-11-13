package kz.iitu.spring_lab_01.security;

import kz.iitu.spring_lab_01.catalog.BookService;
import kz.iitu.spring_lab_01.catalog.Genre;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityAccessTest {

    @Autowired MockMvc mvc;
    @Autowired BookService bookService;
    @Autowired PasswordEncoder encoder;
    @Autowired JdbcTemplate jdbc;

    // ---------- Task 3: the access matrix ----------

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/books")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mvc.perform(get("/admin")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void userCanReadButNotAdminister() throws Exception {
        mvc.perform(get("/books")).andExpect(status().isOk());
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
        mvc.perform(post("/books").with(csrf()).param("title", "X").param("genre", "NOVEL").param("year", "2020"))
                .andExpect(status().isForbidden());
        mvc.perform(get("/nowhere")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void adminIsAllowed() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk());
        mvc.perform(post("/books").with(csrf()).param("title", "Admin book").param("genre", "NOVEL").param("year", "2021"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/books"));
        mvc.perform(get("/nowhere")).andExpect(status().isForbidden()); // denyAll() applies to everybody
    }

    // ---------- Task 3.2: method security ----------

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void preAuthorizeProtectsServiceEvenWithoutController() {
        assertThatThrownBy(() -> bookService.delete(1L)).isInstanceOf(AuthorizationDeniedException.class);
        assertThatThrownBy(() -> bookService.create("Bypass", Genre.NOVEL, 2020))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    // ---------- Task 4: CSRF ----------

    @Test
    @WithMockUser(username = "admin", roles = {"USER", "ADMIN"})
    void postWithoutCsrfTokenIsForbidden() throws Exception {
        mvc.perform(post("/books").param("title", "Forged").param("genre", "NOVEL").param("year", "2020"))
                .andExpect(status().isForbidden());
        Integer forged = jdbc.queryForObject("select count(*) from book where title = 'Forged'", Integer.class);
        assertThat(forged).isZero();
    }

    // ---------- Task 2: database users + BCrypt ----------

    @Test
    void databaseUsersLogInWithBcryptPasswords() throws Exception {
        mvc.perform(formLogin().user("user").password("user123")).andExpect(authenticated().withRoles("USER"));
        mvc.perform(formLogin().user("admin").password("admin123")).andExpect(authenticated().withRoles("ADMIN", "USER"));
        mvc.perform(formLogin().user("user").password("wrong")).andExpect(unauthenticated());
    }

    @Test
    void noPlaintextIsStoredAndHashesAreSalted() {
        List<String> hashes = jdbc.queryForList("select password_hash from app_user", String.class);
        assertThat(hashes).allMatch(h -> h.startsWith("$2a$10$") && h.length() == 60);
        assertThat(hashes).noneMatch(h -> h.contains("user123") || h.contains("admin123"));

        String h1 = encoder.encode("user123");
        String h2 = encoder.encode("user123");
        System.out.println("[TASK 2] user123 -> " + h1);
        System.out.println("[TASK 2] user123 -> " + h2);
        assertThat(h1).isNotEqualTo(h2);
        assertThat(encoder.matches("user123", h1)).isTrue();
        assertThat(encoder.matches("user123", h2)).isTrue();
    }

    @Test
    void disabledUserCannotLogIn() throws Exception {
        jdbc.update("update app_user set enabled = false where username = 'user'");
        try {
            mvc.perform(formLogin().user("user").password("user123")).andExpect(unauthenticated());
        } finally {
            jdbc.update("update app_user set enabled = true where username = 'user'");
        }
    }
}
