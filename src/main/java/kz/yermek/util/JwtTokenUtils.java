package kz.yermek.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import kz.yermek.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
    private static final String ACCESS_SECRET_KEY = "eNWhaPA+OWfE/gttYsOIA+LnqHaVOsgGgZjFEPMqZJJ5cYnVdWmoWChe+T31DwCs";
    private static final String REFRESH_SECRET_KEY = "W32iXD8bCt5jRECb5GqWu5gAMIj/QyJdH0LZKag3m3ogMJMV9bzEwGqwm6dzbliU";
    private static final String JWT_SECRET_KEY = "DFMWu6DjAJEmtZLetpXbnQKsJqnRzTcBXlF2WF/FWwlqDxqyj8GKMoU8BnCADE7t";

    private static SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(ACCESS_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    private static SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(REFRESH_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roleList = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roleList);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(Instant.now().toEpochMilli()))
                .setExpiration(new Date(Instant.now().plus(10, ChronoUnit.MINUTES).toEpochMilli()))
                .signWith(getAccessKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(Instant.now().toEpochMilli()))
                .setExpiration(new Date(Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli()))
                .signWith(getRefreshKey())
                .compact();
    }

    public Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User user) {
                return user.getId();
            } else {
                throw new IllegalArgumentException("Principal is not an instance of User");
            }
        }
        return null;
    }
//    public String getEmail(String token) {
//        return  Jwts.parser()
//                .verifyWith(getAccessKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getSubject();
//    }

    public String getEmail(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getAccessKey())
                    .build()
                    .parseClaimsJws(token);
            return claimsJws.getBody().getSubject();
        } catch (Exception e) {
            // Handle token parsing errors
            System.out.println("Error parsing JWT token: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    public String getEmailFromRefreshToken(String refreshToken) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getRefreshKey())
                    .build()
                    .parseClaimsJws(refreshToken);
            return claimsJws.getBody().getSubject();
        } catch (Exception e) {
            // Handle token parsing errors
            System.out.println("Error parsing refresh token: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }
    private Claims getAllClaimsFromToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(getAccessKey())
                    .build()
                    .parseClaimsJws(token);
            return claimsJws.getBody();
        } catch (Exception e) {
            // Handle token parsing errors
            System.out.println("Error parsing token: " + e.getMessage());
            return null; // or throw a custom exception
        }
    }

//    public String getEmailFromRefreshToken(String refreshToken) {
//        try {
//            return Jwts.parserBuilder()
//                    .setSigningKey(getRefreshKey())
//                    .build()
//                    .parseClaimsJws(refreshToken)
//                    .getBody()
//                    .getSubject();
//        } catch (ExpiredJwtException e) {
//            // Handle token expiration gracefully
//            System.out.println("Refresh token has expired: " + e.getMessage());
//            // Example: Redirect the user to re-authenticate or generate a new token
//            return null; // or perform appropriate action
//        } catch (SignatureException e) {
//            // Handle signature mismatch or invalid token signature
//            System.out.println("Invalid token signature: " + e.getMessage());
//            // Example: Log the error and reject the token
//            return null; // or perform appropriate action
//        } catch (MalformedJwtException e) {
//            // Handle malformed JWT token
//            System.out.println("Malformed JWT token: " + e.getMessage());
//            // Example: Log the error and reject the token
//            return null; // or perform appropriate action
//        } catch (Exception e) {
//            // Handle other exceptions such as invalid token format or parsing errors
//            System.out.println("Error parsing refresh token: " + e.getMessage());
//            return null; // or throw a custom exception
//        }
//    }


//    private Claims getAllClaimsFromToken(String token) {
//        try {
//            return Jwts.parserBuilder()
//                    .setSigningKey(getAccessKey())
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//        } catch (ExpiredJwtException e) {
//            // Handle token expiration here
//            System.out.println("JWT token has expired: " + e.getMessage());
//            return null; // or throw a custom exception
//        } catch (Exception e) {
//            // Handle other exceptions such as invalid token format or signature
//            System.out.println("Error parsing JWT token: " + e.getMessage());
//            return null; // or throw a custom exception
//        }
//    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails user) {
        final String username = getEmail(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

}
