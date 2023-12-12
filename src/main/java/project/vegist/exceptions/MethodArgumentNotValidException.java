package project.vegist.exceptions;

import org.springframework.http.HttpStatus;

public class MethodArgumentNotValidException extends AppException implements HttpStatusProvider {
    private final HttpStatus status;

    public MethodArgumentNotValidException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
