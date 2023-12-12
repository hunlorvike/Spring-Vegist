package project.vegist.exceptions;

import org.springframework.http.HttpStatus;

public interface HttpStatusProvider {
    HttpStatus getStatus();
}

