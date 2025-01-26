package fr.thomasdindin.api_starter.authentication.errors;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
