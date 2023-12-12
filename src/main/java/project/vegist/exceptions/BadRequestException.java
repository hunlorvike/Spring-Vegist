package project.vegist.exceptions;

public class BadRequestException extends AppException {
    public BadRequestException(String message) {
        super(message);
    }
}

// Thông báo khi yêu cầu của người dùng không hợp lệ.