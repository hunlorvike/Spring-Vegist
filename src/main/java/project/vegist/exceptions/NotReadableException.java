package project.vegist.exceptions;

import org.springframework.http.HttpStatus;

public class NotReadableException extends AppException implements HttpStatusProvider {

    private final HttpStatus status;

    public NotReadableException(String data, HttpStatus status) {
        super(String.format("Cannot read data %s", data));
        this.status = status;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
