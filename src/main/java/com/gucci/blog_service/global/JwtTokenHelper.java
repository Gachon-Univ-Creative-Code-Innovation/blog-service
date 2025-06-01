package com.gucci.blog_service.global;

import com.gucci.common.exception.CustomException;
import com.gucci.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;


@Component
public class JwtTokenHelper {
    private final String secretKey;
    private final Key SECRET_KEY;

    public JwtTokenHelper(
            @Value("${jwt.secret}") String secretKey
    ) {
        this.secretKey = secretKey;
        this.SECRET_KEY = new SecretKeySpec(
                java.util.Base64.getDecoder().decode(secretKey),
                SignatureAlgorithm.HS512.getJcaName()
        );
    }

    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
        }
    }

    private String getJwtToken(String token) {
        return token.replace("Bearer", "").trim();
    }

    public String getEmailFromToken(String token) {
        token = getJwtToken(token);
        return extractClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        token = getJwtToken(token);
        return extractClaims(token).get("user_id", Long.class);
    }

    public String getRoleFromToken(String token) {
        token = getJwtToken(token);
        return extractClaims(token).get("role", String.class);
    }

    public String getNicknameFromToken(String token) {
        token = getJwtToken(token);
        return extractClaims(token).get("nickname", String.class);
    }
}

