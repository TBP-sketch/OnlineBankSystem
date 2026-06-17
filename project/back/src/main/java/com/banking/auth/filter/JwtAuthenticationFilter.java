package com.banking.auth.filter;

import com.bank.account.config.BankUserDetails;
import com.banking.auth.service.CustomUserDetailsService;
import com.banking.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @weijh
 * JWT 认证过滤器
 * 每次请求执行一次，解析 Bearer Token 并设置 SecurityContext
 */
@Component  //Spring 自动注册为 Bean，供 SecurityConfig 注入
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    //标准 HTTP 头格式
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        // 有 Token 且尚未认证时才处理
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String username = jwtUtil.extractUsername(token);
                if (username != null) {
                    // 查库，以最新账号状态为准
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    // Token 合法且账号未禁用、未锁定
                    if (jwtUtil.isTokenValid(token, userDetails)
                            && userDetails.isEnabled()
                            && userDetails.isAccountNonLocked()) {
                        Long userId = jwtUtil.extractUserId(token);
                        BankUserDetails principal = new BankUserDetails(
                                userId, username, userDetails.getAuthorities());
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        principal, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        // 写入上下文，后续 .authenticated() / @PreAuthorize 生效
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("JWT 认证成功: user={} path={}", username, request.getRequestURI());
                    } else if (jwtUtil.isTokenValid(token, userDetails)) {
                        // Token 仍有效但账号异常，故意不设认证
                        log.debug("JWT 有效但用户已冻结/禁用，拒绝认证: user={}", username);
                    }
                }
            } catch (Exception e) {
                log.debug("JWT 认证失败: {} path={}", e.getMessage(), request.getRequestURI());
                // 不中断请求，由 Security 统一返回 401
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
