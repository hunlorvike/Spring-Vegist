package project.vegist.responses;

import java.util.List;

public class ErrorResponse<T> extends BaseResponse<T> {
    public ErrorResponse(String message) {
        super("failed", message, null);
    }

    public ErrorResponse(String message, T data) {
        super("failed", message, data);
    }

    public ErrorResponse(List<String> messages) {
        super("failed", messages.isEmpty() ? "Failed" : messages.get(0), null);
    }

}
