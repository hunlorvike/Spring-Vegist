package project.vegist.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceExistException extends AppException implements HttpStatusProvider {
    private final HttpStatus status;

    public ResourceExistException(String resource, HttpStatus status) {
        super(String.format("Resource %s already exists", resource));
        this.status = status;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
