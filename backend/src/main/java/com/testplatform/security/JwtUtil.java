package com.testplatform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具类（jjwt 0.12.x，HS256）
 * <p>
 * access token 载荷：userId / username / roles / perms，有效期 2 小时；
 * refresh token 载荷：userId / username，有效期 7 天。
 */
@Component
public class JwtUtil {

    private final SecretKey key;

    private final long accessExpireSeconds;

    private final long refreshExpireSeconds;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-expire-seconds}") long accessExpireSeconds,
                   @Value("${jwt.refresh-expire-seconds}") long refreshExpireSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireSeconds = accessExpireSeconds;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    /**
     * 签发 access token
     */
    public String generateAccessToken(Long userId, String username, List<String> roles, List<String> perms) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("username", username)
                .claim("roles", roles)
                .claim("perms", perms)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessExpireSeconds * 1000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 签发 refresh token
     */
    public String generateRefreshToken(Long userId, String username) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("userId", userId)
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshExpireSeconds * 1000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析并校验 token（签名 + 过期），返回载荷
     * <p>
     * 过期抛 {@link io.jsonwebtoken.ExpiredJwtException}，签名错误/格式非法抛 {@link io.jsonwebtoken.JwtException}
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从载荷取 userId（subject 存 userId 字符串，避免数字类型转换问题）
     */
    public Long getUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从载荷取 username
     */
    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    /**
     * 从载荷取 roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        Object roles = claims.get("roles");
        return roles == null ? List.of() : (List<String>) roles;
    }

    /**
     * 从载荷取 perms
     */
    @SuppressWarnings("unchecked")
    public List<String> getPerms(Claims claims) {
        Object perms = claims.get("perms");
        return perms == null ? List.of() : (List<String>) perms;
    }
}