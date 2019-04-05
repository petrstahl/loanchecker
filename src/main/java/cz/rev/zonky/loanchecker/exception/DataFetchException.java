package cz.rev.zonky.loanchecker.exception;

/**
 * Represents exception during fetching data from server.
 *
 * @author Petr Stahl [petr.stahl@gmail.com]
 */
public class DataFetchException extends RuntimeException {

    public DataFetchException(String message) {
        super(message);
    }

    public DataFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
