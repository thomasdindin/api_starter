package fr.thomasdindin.api_starter.authentication.errors;

public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException(String message) {
        super(message);
    }
}
