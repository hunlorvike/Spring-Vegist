package project.vegist.responses;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public class SuccessResponse<T> extends BaseResponse<T>  {
    public SuccessResponse(T data) {
        super("success", null, data);
    }

    public SuccessResponse(T data, String message) {
        super("success", message, data);
    }
}


