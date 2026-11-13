package kz.iitu.spring_lab_01.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Task 2.2: prints BCrypt hashes for the two teaching passwords. Run it twice and compare. */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        var encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("user123"));
        System.out.println(encoder.encode("admin123"));
    }
}
