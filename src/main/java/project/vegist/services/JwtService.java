package project.vegist.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import project.vegist.entities.User;
import project.vegist.models.CustomUserDetail;
import project.vegist.repositories.UserRepository;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class JwtService {
    private final UserRepository userRepository;
    private final String secretKey;
    private final Long expiration;

    public JwtService(UserRepository userRepository,
                      @Value("${jwt.secret.key}") String secretKey,
                      @Value("${jwt.expiration}") Long expiration) {
        this.userRepository = userRepository;
        this.secretKey = secretKey;
        this.expiration = expiration;
    }

    private SecretKey getSecret() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    private String getUserAgentFromRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return requestAttributes.getRequest().getHeader("User-Agent");
    }

    public String generateToken(CustomUserDetail customUserDetail) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        Collection<String> roles = customUserDetail.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return "Bearer " + Jwts.builder()
                .setHeaderParam("User-Agent", getUserAgentFromRequest())
                .claim("typeToken", "Bearer")
                .claim("userId", customUserDetail.getUser().getId())
                .claim("email", customUserDetail.getUser().getEmail())
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSecret(), SignatureAlgorithm.HS512)
                .compact();
    }


    public String refreshExpiredToken(String expiredToken) {
        if (!isTokenValid(expiredToken)) {
            Claims claims = getClaims(expiredToken);
            String userId = claims.getSubject();

            User user = userRepository.findById(Long.valueOf(userId))
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

            CustomUserDetail userDetails = new CustomUserDetail(user);

            return generateToken(userDetails);
        }

        return expiredToken;
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSecret()).build().parseClaimsJws(removeBearerPrefix(token));
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("JWT token is null or empty");
        }

        String cleanedToken = removeBearerPrefix(token);

        Claims claims = Jwts.parserBuilder().setSigningKey(getSecret()).build().parseClaimsJws(cleanedToken).getBody();

        // Get the user ID from the "userId" claim
        Long userId = claims.get("userId", Long.class);

        if (userId == null) {
            throw new IllegalArgumentException("User ID not found in the token");
        }

        return userId;
    }

    public String getEmailFromToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("JWT token is null or empty");
        }

        String cleanedToken = removeBearerPrefix(token);

        Claims claims = Jwts.parserBuilder().setSigningKey(getSecret()).build().parseClaimsJws(cleanedToken).getBody();

        String email = claims.get("email", String.class);

        if (email == null) {
            throw new IllegalArgumentException("User ID not found in the token");
        }

        return email;
    }


    public Claims getClaims(String token) {
        try {
            String tokenWithoutPrefix = removeBearerPrefix(token);
            return Jwts.parserBuilder().setSigningKey(getSecret()).build().parseClaimsJws(tokenWithoutPrefix).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token or expired token", e);
        }
    }

    private String removeBearerPrefix(String token) {
        return token.replace("Bearer ", "");
    }
}