package com.testplatform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.common.ErrorCode;
import com.testplatform.common.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT 认证过滤器：解析 Authorization: Bearer &lt;token&gt;，校验签名与过期
 * <p>
 * - 白名单路径直接放行（由 SecurityConfig 决定是否放行）
 * - 无 token：放行，由 Security 的 AuthenticationEntryPoint 返回 40100
 * - token 过期：直接返回 40101
 * - token 无效（签名错误/格式非法）：直接返回 40100
 * - token 有效：写入 SecurityContext + LoginUser
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 白名单路径跳过 token 解析（login/refresh 凭 body 凭证，health 匿名）
        if (isWhitelist(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            // 无 token：放行，由 Security 认证入口统一返回 40100
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = jwtUtil.getUserId(claims);
            String username = jwtUtil.getUsername(claims);
            List<String> roles = jwtUtil.getRoles(claims);
            List<String> perms = jwtUtil.getPerms(claims);

            // 写入 Spring Security 上下文（认证通过）
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 写入业务上下文
            LoginUser.set(new LoginUser(userId, username, roles, perms));

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        } finally {
            LoginUser.clear();
        }
    }

    /**
     * 白名单：POST /api/v1/auth/login、POST /api/v1/auth/refresh、GET /api/health
     */
    private boolean isWhitelist(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("/api/health".equals(path)) {
            return true;
        }
        return "POST".equalsIgnoreCase(method)
                && ("/api/v1/auth/login".equals(path) || "/api/v1/auth/refresh".equals(path));
    }

    /**
     * 写统一 JSON 错误响应
     */
    private void writeError(HttpServletResponse response, int httpStatus, ErrorCode errorCode) throws IOException {
        response.setStatus(httpStatus);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(errorCode)));
    }
}