package project.vegist.dtos;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StandardErrorDTO {
    private Integer status;
    private String error;
    private String message;
    private String path;

    public StandardErrorDTO(HttpStatus status, Throwable ex, HttpServletRequest request) {
        this.status = status.value();
        this.error = ex.getClass().getSimpleName();
        this.message = ex.getMessage();
        this.path = request.getRequestURI();
    }
}
