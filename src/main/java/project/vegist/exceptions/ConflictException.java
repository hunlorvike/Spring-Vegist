package project.vegist.exceptions;

public class ConflictException extends AppException {
    public ConflictException(String message) {
        super(message);
    }
}

// Được sử dụng khi có xung đột dữ liệu, ví dụ: tạo một tài nguyên đã tồn tại.
