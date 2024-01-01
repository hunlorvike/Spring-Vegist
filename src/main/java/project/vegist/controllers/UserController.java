package project.vegist.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import project.vegist.exceptions.ResourceExistException;
import project.vegist.repositories.UserRepository;
import project.vegist.requests.LoginRequest;
import project.vegist.requests.RegisterRequest;
import project.vegist.responses.BaseResponse;
import project.vegist.responses.ErrorResponse;
import project.vegist.responses.SuccessResponse;
import project.vegist.services.UserService;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<BaseResponse<String>> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String accessToken = userService.login(loginRequest);

            return ResponseEntity.ok(new SuccessResponse<>(accessToken, "Login successfully"));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse<>(e.getMessage()));
        }
    }

    @PostMapping("/auth/register")
    public ResponseEntity<BaseResponse<Void>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new ResourceExistException(registerRequest.getEmail(), HttpStatus.CONFLICT);
            }

            boolean isRegister = userService.register(registerRequest);

            return isRegister
                    ? new ResponseEntity<>(new SuccessResponse<>(null, "User registered successfully"), HttpStatus.CREATED)
                    : ResponseEntity.badRequest().build();
        } catch (ResourceExistException e) {
            return ResponseEntity.status(e.getStatus())
                    .body(new ErrorResponse<>(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse<>("Error during user registration: " + e.getMessage()));
        }
    }

}
