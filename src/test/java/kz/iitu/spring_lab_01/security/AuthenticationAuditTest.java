package kz.iitu.spring_lab_01.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

/** Variant 10: every authentication success and failure leaves exactly one audit row. */
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationAuditTest {

    @Autowired MockMvc mvc;
    @Autowired AuthAuditRepository audit;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        audit.deleteAll();
    }

    @Test
    void successAndFailureEventsAreAudited() throws Exception {
        mvc.perform(formLogin().user("admin").password("admin123"));      // success
        mvc.perform(formLogin().user("user").password("wrong-password")); // bad password
        mvc.perform(formLogin().user("nobody").password("whatever"));     // unknown user
        jdbc.update("update app_user set enabled = false where username = 'user'");
        try {
            mvc.perform(formLogin().user("user").password("user123"));    // disabled account
        } finally {
            jdbc.update("update app_user set enabled = true where username = 'user'");
        }

        List<AuthAuditEvent> rows = audit.findAll().stream()
                .sorted((a, b) -> Long.compare(a.getId(), b.getId())).toList();
        rows.forEach(r -> System.out.printf("[VARIANT 10] %-7s %-42s success=%-5s reason=%s%n",
                r.getUsername(), r.getEventType(), r.isSuccess(), r.getReason()));

        assertThat(rows).hasSize(4);
        assertThat(rows).extracting(AuthAuditEvent::getEventType).containsExactly(
                "AuthenticationSuccessEvent",
                "AuthenticationFailureBadCredentialsEvent",
                "AuthenticationFailureBadCredentialsEvent",
                "AuthenticationFailureDisabledEvent");
        assertThat(rows).extracting(AuthAuditEvent::isSuccess).containsExactly(true, false, false, false);
        assertThat(rows).extracting(AuthAuditEvent::getUsername).containsExactly("admin", "user", "nobody", "user");
    }
}
