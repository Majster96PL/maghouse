package com.example.maghouse.auth.login.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongSupplier;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final SecretPropertiesReader secretPropertiesReader;

    public String getToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }
    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, getJwtExpiration());
    }

    public String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigntInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken (UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, getRefreshExpiration());
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        final String userEmail = extractUserEmail(token);
        return (userEmail.equals(userDetails.getUsername())) && !isExpiredToken(token);
    }

    private boolean isExpiredToken(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractUserEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        if (claims == null ){
            throw new IllegalArgumentException("Claims cannot be null");
        }
        return claimsTFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSigntInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error extracting all claims from token");
        }
    }

    private Key getSigntInKey() {
        try {
            String secretKey = secretPropertiesReader.readSecretKey();
            if (secretKey == null) {
                throw new IllegalArgumentException("Secret key cannot be null!");
            }
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid secret key format", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected error while getting sign-in key", e);
        }
    }

    private long getJwtExpiration() {
        return handleException(() -> {
            String expirationString;
            try {
                expirationString = String.valueOf(secretPropertiesReader.readJwtExpiration());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (expirationString.isEmpty()) {
                throw new IllegalArgumentException("JWT expiration value cannot be null or empty!");
            }
            return Long.parseLong(expirationString);
        });
    }

    private long getRefreshExpiration() {
        return handleException(() -> {
            String expirationString;
            try {
                expirationString = String.valueOf(secretPropertiesReader.readRefreshExpiration());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (expirationString.isEmpty()) {
                throw new IllegalArgumentException("Refresh expiration value cannot be null or empty!");
            }
            return Long.parseLong(expirationString);
        });
    }

    private long handleException(LongSupplier supplier) {
        try {
            return supplier.getAsLong();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return 0;
        }
    }
}

