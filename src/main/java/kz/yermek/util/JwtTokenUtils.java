package kz.yermek.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

import static javax.crypto.Cipher.SECRET_KEY;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
    private static final String ACCESS_SECRET_KEY = "eNWhaPA+OWfE/gttYsOIA+LnqHaVOsgGgZjFEPMqZJJ5cYnVdWmoWChe+T31DwCs";
    private static final String REFRESH_SECRET_KEY = "W32iXD8bCt5jRECb5GqWu5gAMIj/QyJdH0LZKag3m3ogMJMV9bzEwGqwm6dzbliU";

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
                .toList();
        claims.put("roles", roleList);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date(Instant.now().toEpochMilli()))
                .setExpiration(new Date(Instant.now().plus(2, ChronoUnit.MINUTES).toEpochMilli()))
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
            if (principal instanceof User) {
                User user = (User) principal;
                return user.getId();
            } else {
                throw new IllegalArgumentException("Principal is not an instance of User");
            }
        }
        return null;
    }

    public String getEmail(String token) {
        return  Jwts.parser()
                .verifyWith(getAccessKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public String getEmailFromRefreshToken(String refreshToken) {
        return Jwts.parser()
                .verifyWith(getRefreshKey())
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload()
                .getSubject();
    }
    private Claims getAllClaimsFromToken(String token){
        return Jwts
                .parser()
                .setSigningKey(getAccessKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

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
