package project.vegist.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;

public class ResponseUtils {
    public static <T> ResponseEntity<BaseResponse<T>> createSuccessResponse(T data) {
        return ResponseEntity.ok(new SuccessResponse<>(data));
    }

    public static ResponseEntity<BaseResponse<?>> createNotFoundResponse(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse<>(message, null));
    }

    public static ResponseEntity<BaseResponse<?>> createErrorResponse(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse<>(message, null));
    }

    public static ResponseEntity<BaseResponse<?>> createCustomErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse<>(message, null));
    }
}
