package cart.exception;

public class BaseException extends RuntimeException {

    private final ExceptionType exceptionType;

    public BaseException(ExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    public ExceptionType getExceptionType() {
        return exceptionType;
    }
}
