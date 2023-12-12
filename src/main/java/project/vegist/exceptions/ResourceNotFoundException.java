package project.vegist.exceptions;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends AppException implements HttpStatusProvider {
    private final HttpStatus status;

    public ResourceNotFoundException(String resource, Long id, HttpStatus status) {
        super(String.format("%s not found with id: %d", resource, id));
        this.status = status;
    }

    @Override
    public HttpStatus getStatus() {
        return status;
    }
}
