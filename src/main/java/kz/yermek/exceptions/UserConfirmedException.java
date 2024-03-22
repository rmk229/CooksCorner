package kz.yermek.exceptions;

public class UserConfirmedException extends RuntimeException {
    public UserConfirmedException(String message) {
        super(message);
    }
}
