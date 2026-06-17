package com.banking.auth.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * &#064;weijh
 * 认证模块请求 DTO 集合
 */
public class AuthRequest {

    // ===== 注册 =====
    @Data
    public static class Register {

        @NotBlank(message = "用户名不能为空")
        @Size(min = 4, max = 50, message = "用户名长度 4-50 位")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        private String username;

        @NotBlank(message = "密码不能为空")
        @Size(min = 8, max = 100, message = "密码至少 8 位")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "密码须包含大小写字母和数字")
        private String password;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @Size(max = 20, message = "手机号最多 20 位")
        private String phone;

        @Size(max = 100, message = "姓名最多 100 位")
        private String fullName;
    }

    // ===== 登录 =====
    @Data
    public static class Login {

        @NotBlank(message = "用户名不能为空")
        private String username;

        @NotBlank(message = "密码不能为空")
        private String password;
    }

    // ===== OTP 验证（注册邮箱 / 登录 MFA） =====
    @Data
    public static class VerifyOtp {

        @NotBlank(message = "目标不能为空（邮箱或手机号）")
        private String target;

        @NotBlank(message = "OTP 不能为空")
        @Size(min = 6, max = 6, message = "OTP 为 6 位数字")
        @Pattern(regexp = "\\d{6}", message = "OTP 只能包含数字")
        private String code;

        @NotNull(message = "OTP 类型不能为空")
        private String type; // EMAIL_VERIFY / LOGIN_MFA / PASSWORD_RESET
    }

    // ===== 刷新 Token =====
    @Data
    public static class RefreshToken {

        @NotBlank(message = "refreshToken 不能为空")
        private String refreshToken;
    }

    // ===== 修改密码 =====
    @Data
    public static class ChangePassword {

        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 100, message = "密码至少 8 位")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "密码须包含大小写字母和数字")
        private String newPassword;
    }

    // ===== 找回密码：发送 OTP =====
    @Data
    public static class ForgotPassword {

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
    }

    // ===== 找回密码：重置密码 =====
    @Data
    public static class ResetPassword {

        @NotBlank(message = "邮箱不能为空")
        @Email
        private String email;

        @NotBlank(message = "OTP 不能为空")
        @Size(min = 6, max = 6)
        private String code;

        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 100)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "密码须包含大小写字母和数字")
        private String newPassword;
    }

    // ===== 重发 OTP =====
    @Data
    public static class ResendOtp {

        @NotBlank
        private String target;

        @NotBlank
        private String type;
    }
}
