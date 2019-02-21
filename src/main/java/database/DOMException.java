package database;

public class DOMException extends Exception {

    public DOMException(String message, Object... arguments) {
        super(String.format(message, arguments));
    }

}
