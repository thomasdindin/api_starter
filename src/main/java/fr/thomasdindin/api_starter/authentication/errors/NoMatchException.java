package fr.thomasdindin.api_starter.authentication.errors;

public class NoMatchException extends RuntimeException {
    public NoMatchException(String message) {
        super(message);
    }
}
