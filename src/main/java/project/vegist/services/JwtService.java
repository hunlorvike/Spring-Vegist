package project.vegist.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final SecretKey secretKey;
    private final Long expiration;

    @Autowired
    public JwtService(UserRepository userRepository,
                      @Value("${jwt.secret.key}") String secretKey,
                      @Value("${jwt.expiration}") Long expiration) {
        this.userRepository = userRepository;
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.expiration = expiration;
    }

    private String getUserAgentFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        return request.getHeader("User-Agent");
    }

    public String generateToken(CustomUserDetail customUserDetail) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        Collection<String> roles = customUserDetail.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Claims claims = buildClaims(customUserDetail.getUser().getId(), customUserDetail.getUser().getEmail(), roles);

        return "Bearer " + Jwts.builder()
                .setHeaderParam("User-Agent", getUserAgentFromRequest())
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    @Transactional
    public String refreshAccessToken(String refreshToken) {
        if (isTokenValid(refreshToken)) {
            Claims claims = getClaims(refreshToken);
            String userEmail = claims.getSubject();

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

            CustomUserDetail userDetails = new CustomUserDetail(user);
            String newAccessToken = generateToken(userDetails);

            return newAccessToken;
        }

        return null;
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    private Claims buildClaims(Long userId, String email, Collection<String> roles) {
        Claims claims = Jwts.claims();
        claims.put("typeToken", "Bearer");
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("roles", roles);

        return claims;
    }

    @Transactional
    public String refreshExpiredToken(String expiredToken) {
        if (!isTokenValid(expiredToken)) {
            Claims claims = getClaims(expiredToken);
            String userEmail = claims.getSubject();

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

            // Generate a new access token
            CustomUserDetail userDetails = new CustomUserDetail(user);
            String newAccessToken = generateToken(userDetails);

            // Generate a new refresh token
            String newRefreshToken = generateRefreshToken(user);

            // Save the new refresh token in the database
            user.setRefreshToken(newRefreshToken);
            userRepository.save(user);

            return newAccessToken;
        }

        return expiredToken;
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(removeBearerPrefix(token));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new IllegalArgumentException("JWT token is null or empty");
        }

        String cleanedToken = removeBearerPrefix(token);

        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(cleanedToken).getBody();

        // Get the user ID from the "userId" claim
        return claims.get("userId", Long.class);
    }

    public String getEmailFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new IllegalArgumentException("JWT token is null or empty");
        }

        String cleanedToken = removeBearerPrefix(token);

        Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(cleanedToken).getBody();

        return claims.get("email", String.class);
    }

    public Claims getClaims(String token) {
        try {
            String tokenWithoutPrefix = removeBearerPrefix(token);
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(tokenWithoutPrefix).getBody();
        } catch (Exception e) {
            throw new RuntimeException("Invalid token or expired token", e);
        }
    }

    private String removeBearerPrefix(String token) {
        return token.replace("Bearer ", "");
    }
}