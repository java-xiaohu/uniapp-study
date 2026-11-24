package com.hzy.aillm.controller;

import com.hzy.aillm.model.LoginRequest;
import com.hzy.aillm.model.RefreshRequest;
import com.hzy.aillm.model.RegisterRequest;
import com.hzy.aillm.model.TokenResult;
import com.hzy.aillm.model.UserInfoResult;
import com.hzy.aillm.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录，返回双令牌 */
    @PostMapping("/login")
    public TokenResult login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** 注册，默认绑定 USER 角色 */
    @PostMapping("/register")
    public UserInfoResult register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** 登出：撤销当前访问令牌与刷新令牌 */
    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
    }

    /** 刷新令牌换新双令牌（旧刷新令牌作废） */
    @PostMapping("/refresh")
    public TokenResult refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    /** 当前登录用户信息 */
    @GetMapping("/me")
    public UserInfoResult me() {
        return authService.me();
    }

    /** 用户列表，仅管理员可访问（RBAC 示例） */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserInfoResult> users() {
        return authService.listUsers();
    }
}