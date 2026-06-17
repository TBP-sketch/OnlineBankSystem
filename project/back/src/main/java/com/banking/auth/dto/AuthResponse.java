package com.banking.auth.dto;

import com.banking.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * weijh
 * 认证模块响应 DTO 集合
 */
public class AuthResponse {

    // ===== 统一 API 响应包装 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiResult<T> {
        private boolean success;
        private String message;
        private T data;
        private LocalDateTime timestamp;

        public static <T> ApiResult<T> ok(T data, String message) {
            return ApiResult.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static <T> ApiResult<T> fail(String message) {
            return ApiResult.<T>builder()
                    .success(false)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // ===== 登录成功响应 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResult {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;       // 秒
        private String tokenType;
        private UserInfo userInfo;

        /** 是否需要 MFA（登录后触发） */
        private boolean requireMfa;
        private String mfaTarget;     // 发送 OTP 的邮箱（脱敏）
    }

    // ===== 注册成功响应 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterResult {
        private Long userId;
        private String username;
        private String email;
        private String message;
        private boolean emailVerificationSent;
    }

    // ===== Token 刷新响应 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenResult {
        private String accessToken;
        private String refreshToken;
        private long expiresIn;
        private String tokenType;
    }

    // ===== 用户信息（供其他模块参考） =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private String status;
        private LocalDateTime lastLoginAt;

        public static UserInfo from(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .status(user.getStatus().name())
                    .lastLoginAt(user.getLastLoginAt())
                    .build();
        }
    }

    // ===== OTP 发送响应 =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OtpResult {
        private boolean sent;
        private String maskedTarget;   // 脱敏目标（如 us***@mail.com）
        private long expiresInSeconds;
        private String message;
    }
}
