package project.vegist.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import project.vegist.services.JwtService;

@RestController
public class TestController {
    @Autowired
    private JwtService jwtService;

    @GetMapping("/token/claims")
    public ResponseEntity<Claims> claims(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(jwtService.getClaims(token));
    }

    @GetMapping("/get-username")
    public ResponseEntity<String> getEmailFromToken(@RequestHeader("Authorization") String token) {
        String email = jwtService.getEmailFromToken(token);
        return ResponseEntity.ok(email);
    }

    @GetMapping("/private/user")
    @PreAuthorize("hasAnyAuthority('ROLE_USER')")
    public String user() {
        return "Đây là role user";
    }

    @GetMapping("/private/admin")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public String admin() {
        return "Đây là role admin";
    }

    @GetMapping("/private/admin-read")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_READ')")
    public ResponseEntity<String> adminREADEndpoint() {
        return ResponseEntity.ok("Đây là role admin read");
    }

    @PostMapping("/private/admin-write")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_WRITE')")
    public ResponseEntity<String> adminWriteEndpoint() {
        return ResponseEntity.ok("Đây là role admin write");
    }

    @PutMapping("/private/admin-edit")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_EDIT')")
    public ResponseEntity<String> adminEditEndpoint() {
        return ResponseEntity.ok("Đây là role admin edit");
    }

    @DeleteMapping("/private/admin-delete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN_DELETE')")
    public ResponseEntity<String> adminDeleteEndpoint() {
        return ResponseEntity.ok("Đây là role admin delete");
    }
}
