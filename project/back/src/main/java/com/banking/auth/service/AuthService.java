package com.banking.auth.service;

import com.banking.auth.dto.AuthRequest;
import com.banking.auth.dto.AuthResponse.*;
import com.banking.auth.entity.OtpRecord.OtpType;
import com.banking.auth.entity.RefreshToken;
import com.banking.auth.entity.User;
import com.banking.auth.exception.AuthException;
import com.banking.auth.repository.UserRepository;
import com.banking.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 核心认证服务
 * 处理注册、登录、Token 刷新、密码修改等业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;

    /** 最大失败次数后锁定 */
    private static final int MAX_FAILED_ATTEMPTS = 5;
    /** 锁定时长（分钟） */
    private static final int LOCK_DURATION_MINUTES = 30;

    // ===== 注册 =====

    @Transactional
    public RegisterResult register(AuthRequest.Register request) {
        // 唯一性校验
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException.UserAlreadyExistsException("用户名已存在: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException.UserAlreadyExistsException("邮箱已注册: " + request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new AuthException.UserAlreadyExistsException("手机号已注册");
        }
        
        //创建用户对象
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .fullName(request.getFullName())
                .build();

        user = userRepository.save(user);

        // 发送邮箱验证 OTP
        otpService.generateAndSend(user.getEmail(), OtpType.EMAIL_VERIFY);

        //返回注册结果
        return RegisterResult.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .message("注册成功，请查收验证码邮件完成邮箱验证")
                .emailVerificationSent(true)
                .build();
    }

    // ===== 邮箱验证模块 =====

    @Transactional
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException.UserNotFoundException("邮箱不存在: " + email));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new AuthException.InvalidOtpException("邮箱已验证，无需重复操作");
        }

        //校验OTP
        otpService.verify(email, code, OtpType.EMAIL_VERIFY);
        //更新用户邮箱验证状态
        userRepository.verifyEmail(user.getId());
        //打日志
        log.info("用户 {} 邮箱验证成功", user.getUsername());
    }

    // ===== 登录 =====

    @Transactional(noRollbackFor = {
            AuthException.InvalidCredentialsException.class,
            AuthException.AccountLockedException.class
    })
    public LoginResult login(AuthRequest.Login request, String clientInfo) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException.InvalidCredentialsException("用户名或密码错误"));

        // 账户状态检查
        checkAccountStatus(user);

        // 密码校验
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new AuthException.InvalidCredentialsException("用户名或密码错误");
        }

        // 登录成功，重置失败计数
        userRepository.resetLoginAttempts(user.getId(), LocalDateTime.now());

        // 生成 Tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user, clientInfo);

        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .tokenType("Bearer")
                .userInfo(UserInfo.from(user))
                .requireMfa(false)
                .build();
    }

    // ===== 刷新 Token =====

    @Transactional
    public TokenResult refreshToken(String refreshTokenValue, String clientInfo) {
        RefreshToken oldToken = refreshTokenService.verify(refreshTokenValue);
        User user = oldToken.getUser();

        // 撤销旧 Refresh Token（旋转机制）
        refreshTokenService.revoke(refreshTokenValue);

        // 生成新 Tokens
        String newAccessToken = jwtUtil.generateAccessToken(user);
        RefreshToken newRefreshToken = refreshTokenService.create(user, clientInfo);

        return TokenResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .tokenType("Bearer")
                .build();
    }

    // ===== 登出 =====

    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenService.revoke(refreshTokenValue);
    }

    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenService.revokeAll(userId);
    }

    // ===== 修改密码 =====

    @Transactional
    public void changePassword(Long userId, AuthRequest.ChangePassword request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFoundException("用户不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AuthException.InvalidCredentialsException("旧密码不正确");
        }

        userRepository.updatePassword(userId, passwordEncoder.encode(request.getNewPassword()));
        // 修改密码后撤销所有 refresh token（强制重新登录）
        refreshTokenService.revokeAll(userId);
        log.info("用户 {} 修改密码成功", user.getUsername());
    }

    // ===== 找回密码：发送 OTP =====

    @Transactional
    public OtpResult forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException.UserNotFoundException("该邮箱未注册"));

        // 防止频繁发送
        if (otpService.hasValidOtp(email, OtpType.PASSWORD_RESET)) {
            throw new AuthException.TooManyOtpRequestsException("验证码已发送，请等待 5 分钟后重试");
        }

        return otpService.generateAndSend(email, OtpType.PASSWORD_RESET);
    }

    // ===== 找回密码：重置密码 =====

    @Transactional
    public void resetPassword(AuthRequest.ResetPassword request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException.UserNotFoundException("该邮箱未注册"));

        otpService.verify(request.getEmail(), request.getCode(), OtpType.PASSWORD_RESET);

        userRepository.updatePassword(user.getId(), passwordEncoder.encode(request.getNewPassword()));
        refreshTokenService.revokeAll(user.getId());
        log.info("用户 {} 重置密码成功", user.getUsername());
    }

    // ===== 重发 OTP =====

    @Transactional
    public OtpResult resendOtp(String target, OtpType type) {
        if (otpService.hasValidOtp(target, type)) {
            throw new AuthException.TooManyOtpRequestsException("验证码已发送，请等待 5 分钟后重试");
        }
        return otpService.generateAndSend(target, type);
    }

    // ===== 大额转账 OTP =====

    @Transactional
    public OtpResult sendTransferOtp(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFoundException("用户不存在"));
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new AuthException.InvalidOtpException("账户未绑定邮箱，无法发送验证码");
        }
        if (otpService.hasValidOtp(user.getEmail(), OtpType.TRANSFER_VERIFY)) {
            throw new AuthException.TooManyOtpRequestsException(
                    "验证码已发送至邮箱，请查收或等待过期后重试");
        }
        return otpService.generateAndSend(user.getEmail(), OtpType.TRANSFER_VERIFY);
    }

    public void verifyTransferOtp(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFoundException("用户不存在"));
        otpService.verify(user.getEmail(), code, OtpType.TRANSFER_VERIFY);
    }

    /**
     * 校验当前用户的登录密码（用于取款等敏感操作）
     */
    public void verifyLoginPassword(Long userId, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("请输入登录密码");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFoundException("用户不存在"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("登录密码不正确");
        }
    }

    // ===== 私有方法 =====

    /**
     * 校验用户处于可交易状态（供交易、开户等接口调用）
     */
    public void verifyUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFoundException("用户不存在"));
        checkAccountStatus(user);
    }

    private void checkAccountStatus(User user) {
        if (user.getStatus() == User.UserStatus.PENDING_VERIFY) {
            throw new AuthException.AccountNotVerifiedException("请先完成邮箱验证");
        }
        if (user.getStatus() == User.UserStatus.DISABLED) {
            throw new AuthException.AccountLockedException("账户已被禁用，请联系管理员");
        }
        if (user.getStatus() == User.UserStatus.LOCKED) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new AuthException.AccountLockedException(
                        "账户已锁定至 " + user.getLockedUntil() + "，请稍后重试");
            }
            throw new AuthException.AccountLockedException("账户已被冻结，请联系管理员");
        }
    }

    private void handleFailedLogin(User user) {
        userRepository.incrementFailedAttempts(user.getId());
        int attempts = user.getFailedLoginAttempts() + 1;
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES);
            userRepository.lockAccount(user.getId(), lockUntil);
            log.warn("用户 {} 登录失败 {} 次，已锁定至 {}", user.getUsername(), attempts, lockUntil);
            throw new AuthException.AccountLockedException(
                    "账户已因多次登录失败被锁定 " + LOCK_DURATION_MINUTES + " 分钟");
        }
    }
}
