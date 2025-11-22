package profIT.exception;

public class StatsProcessingException extends RuntimeException {
    public StatsProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatsProcessingException(String message) {
        super(message);
    }
}
