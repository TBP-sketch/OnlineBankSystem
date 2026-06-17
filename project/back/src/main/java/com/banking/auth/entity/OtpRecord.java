package com.banking.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OTP 记录实体，weijh
 * 存储一次性验证码（注册验证 / 登录二步验证 / 找回密码）
 */
@Entity
@Table(name = "otp_records", indexes = {
        @Index(name = "idx_otp_target", columnList = "target"),
        @Index(name = "idx_otp_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 目标：邮箱或手机号 */
    @Column(nullable = false, length = 100)
    private String target;

    /** 6位数字 OTP */
    @Column(nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpType type;

    /** 是否已使用 */
    @Builder.Default
    private Boolean used = false;

    /** 过期时间 */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !used && !isExpired();
    }

    public enum OtpType {
        EMAIL_VERIFY,       // 注册邮箱验证
        LOGIN_MFA,          // 登录二步验证
        PASSWORD_RESET,     // 密码重置
        TRANSFER_VERIFY     // 大额转账邮箱验证
    }
}
