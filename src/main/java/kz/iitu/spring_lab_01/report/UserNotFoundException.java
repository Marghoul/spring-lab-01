package kz.iitu.spring_lab_01.report;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("User '" + username + "' not found");
    }
}
