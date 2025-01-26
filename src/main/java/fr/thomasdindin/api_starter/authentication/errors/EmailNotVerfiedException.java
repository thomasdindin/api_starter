package fr.thomasdindin.api_starter.authentication.errors;

public class EmailNotVerfiedException extends RuntimeException {
    public EmailNotVerfiedException(String message) {
        super(message);
    }
}
