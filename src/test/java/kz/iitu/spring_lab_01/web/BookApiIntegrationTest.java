package kz.iitu.spring_lab_01.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Task 4: the whole application on a random port against a REAL PostgreSQL 17 in a
 * container. Flyway applies V1..V7 to the empty PostgreSQL database on every run.
 * Authentication: the stateless /api/** chain accepts HTTP Basic, so the test sends
 * the seeded admin / user credentials in the Authorization header.
 *
 * disabledWithoutDocker = true: on a machine without Docker the class is reported as
 * skipped instead of failing the build (see docs/lab12-evidence.md for the evidence).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureRestTestClient
class BookApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired RestTestClient client;

    @Test
    void createsThenReadsBackABook() {
        client.post().uri("/api/books")
              .headers(h -> h.setBasicAuth("admin", "admin123"))
              .contentType(MediaType.APPLICATION_JSON)
              .body("{\"title\":\"Dune\",\"genre\":\"FANTASY\",\"year\":1965}")
              .exchange()
              .expectStatus().isCreated()
              .expectHeader().exists("Location");

        client.get().uri("/api/books")
              .headers(h -> h.setBasicAuth("user", "user123"))
              .exchange()
              .expectStatus().isOk()
              .expectBody()
              .jsonPath("$[?(@.title == 'Dune')].year").isEqualTo(1965);
    }

    @Test
    void ordinaryUserIsForbiddenToCreateAndAnonymousIsUnauthorized() {
        client.post().uri("/api/books")
              .headers(h -> h.setBasicAuth("user", "user123"))
              .contentType(MediaType.APPLICATION_JSON)
              .body("{\"title\":\"Forged\",\"genre\":\"NOVEL\",\"year\":2020}")
              .exchange()
              .expectStatus().isForbidden();

        client.get().uri("/api/books").exchange().expectStatus().isUnauthorized();
    }
}
