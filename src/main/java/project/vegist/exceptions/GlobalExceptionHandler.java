package project.vegist.exceptions;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import project.vegist.responses.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<Void>> handleException(Exception ex) {
        log.error("Exception occurred:", ex);
        ErrorResponse<Void> errorResponse = new ErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse<Void>> handleNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found:", ex);
        ErrorResponse<Void> errorResponse = new ErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessage = "Validation error: ";

        FieldError fieldError = ex.getBindingResult().getFieldError();
        if (fieldError != null) {
            errorMessage += fieldError.getDefaultMessage();
        } else {
            errorMessage += "Unknown error. Please check your input.";
        }

        return ResponseEntity.badRequest().body(new ErrorResponse<>(errorMessage));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse<String>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, WebRequest request) {
        String supportedMethods = request.getHeader("Allow");
        String message = "Method '" + ex.getMethod() + "' is not supported for this resource. Supported methods are: " + supportedMethods;
        log.warn(message);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ErrorResponse<>(message));
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse<Void>> handleBadRequestException(BadRequestException ex) {
        log.warn("Bad Request:", ex);
        ErrorResponse<Void> errorResponse = new ErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse<Void>> handleConflictException(ConflictException ex) {
        log.warn("Conflict:", ex);
        ErrorResponse<Void> errorResponse = new ErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse<Void>> handleNotReadableException(NotReadableException ex) {
        log.warn("Not Readable:", ex);
        ErrorResponse<Void> errorResponse = new ErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof JsonParseException || ex.getCause() instanceof JsonMappingException) {
            // Xử lý khi không thể deserializing giá trị từ JSON
            String detailedMessage = "An error occurred while processing the request. ";
            if (ex.getCause() instanceof JsonParseException) {
                JsonParseException jsonParseException = (JsonParseException) ex.getCause();
                detailedMessage += "Error at: " + jsonParseException.getLocation().getSourceRef() +
                        ", Message: " + jsonParseException.getOriginalMessage();
            } else if (ex.getCause() instanceof JsonMappingException) {
                JsonMappingException jsonMappingException = (JsonMappingException) ex.getCause();
                detailedMessage += "Error at: " + jsonMappingException.getPath() +
                        ", Message: " + jsonMappingException.getOriginalMessage();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse<>(detailedMessage));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse<>("An error occurred while processing the request."));
    }


    @ExceptionHandler(ResourceExistException.class)
    public ResponseEntity<ErrorResponse<Void>> handleResourceExistException(ResourceExistException ex) {
        log.warn("Resource Exist:", ex);
        ErrorResponse<Void> errorResponse = new ErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse<Void>> handleUnauthorizedException(UnauthorizedException ex) {
        log.warn("Unauthorized:", ex);
        ErrorResponse<Void> errorResponse = new ErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse<Void>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Method argument type mismatch:", ex);
        ErrorResponse<Void> errorResponse = new ErrorResponse<>(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

}
