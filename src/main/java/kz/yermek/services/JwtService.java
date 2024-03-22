//package kz.yermek.services;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.JWTVerifier;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.exceptions.JWTVerificationException;
//import com.auth0.jwt.interfaces.DecodedJWT;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.time.ZonedDateTime;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Component
//@RequiredArgsConstructor
//public class JwtService {
//
//    @Value("${app.jwt.secret}")
//    private String SECRET_KEY;
//    private final String TOKEN_USERNAME = "username";
//
//    public String generateTokenRefresh(UserDetails userDetails) {
//        return JWT.create()
//                .withClaim(TOKEN_USERNAME, userDetails.getUsername())
//                .withIssuedAt(new Date())
//                .withExpiresAt(Date.from(ZonedDateTime.now().plusMinutes(2200).toInstant()))
//                .sign(Algorithm.HMAC256(SECRET_KEY));
//    }
//    private String buildToken(
//            UserDetails userDetails
//    ) {
//        return JWT.create()
//                .withClaim(TOKEN_USERNAME, userDetails.getUsername())
//                .withIssuedAt(new Date())
//                .withExpiresAt(Date.from(ZonedDateTime.now().plusMinutes(1).toInstant()))
//                .sign(Algorithm.HMAC256(SECRET_KEY));
//    }
//
//    public String generateTokenWithExpiration(UserDetails userDetails, long expirationMillis) {
//        Date issuedAt = new Date();
//        Date expiresAt = new Date(issuedAt.getTime() + expirationMillis);
//
//        return JWT.create()
//                .withClaim(TOKEN_USERNAME, userDetails.getUsername())
//                .withIssuedAt(issuedAt)
//                .withExpiresAt(expiresAt)
//                .sign(Algorithm.HMAC256(SECRET_KEY));
//    }
//
//    public String generateTokenForRequestEmail(UserDetails userDetails) {
//        return JWT.create()
//                .withClaim(TOKEN_USERNAME, userDetails.getUsername())
//                .withIssuedAt(new Date())
//                .withExpiresAt(Date.from(ZonedDateTime.now().plusMinutes(5).toInstant()))
//                .sign(Algorithm.HMAC256(SECRET_KEY));
//    }
//
//    public String validateToken(String token) {
//        JWTVerifier jwtVerifier =
//                JWT.require(Algorithm.HMAC256(SECRET_KEY)).build();
//        DecodedJWT decodedJWT = jwtVerifier.verify(token);
//        return decodedJWT.getClaim(TOKEN_USERNAME).asString();
//    }
//
//    public boolean isValidToken(String token) {
//        try {
//            JWTVerifier jwtVerifier =
//                    JWT.require(Algorithm.HMAC256(SECRET_KEY)).build();
//            jwtVerifier.verify(token);
//            return true;
//        } catch (JWTVerificationException e) {
//            return false;
//        }
//    }
//
//    public String generateRefreshToken(UserDetails userDetails) {
//        return generateTokenRefresh(userDetails);
//    }
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    public String generateToken(UserDetails userDetails) {
//        return generateToken(new HashMap<>(), userDetails);
//    }
//
//
//    private String generateToken(
//            Map<String, Object> extraClaims,
//            UserDetails userDetails
//    ) {
//        return buildToken(userDetails);
//    }
//
//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
//    }
//
//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    private Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//
//    private Claims extractAllClaims(String token) {
//        return Jwts
//                .parser()
//                .setSigningKey(getSignInKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    private Key getSignInKey() {
//        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
//    }
//}
