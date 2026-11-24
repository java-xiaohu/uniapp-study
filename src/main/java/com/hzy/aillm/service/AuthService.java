package com.hzy.aillm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hzy.aillm.entity.SysRole;
import com.hzy.aillm.entity.SysUser;
import com.hzy.aillm.entity.SysUserRole;
import com.hzy.aillm.mapper.SysRoleMapper;
import com.hzy.aillm.mapper.SysUserMapper;
import com.hzy.aillm.mapper.SysUserRoleMapper;
import com.hzy.aillm.model.LoginRequest;
import com.hzy.aillm.model.RefreshRequest;
import com.hzy.aillm.model.RegisterRequest;
import com.hzy.aillm.model.TokenResult;
import com.hzy.aillm.model.UserInfoResult;
import com.hzy.aillm.security.LoginUser;
import com.hzy.aillm.security.SecurityUtils;
import com.hzy.aillm.security.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 认证业务：登录 / 注册 / 登出 / 刷新 / 当前用户信息
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    /** 账号密码登录，成功签发双令牌 */
    public TokenResult login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return tokenService.issueTokens(loginUser.getId(), loginUser.getUsername());
    }

    /** 注册新用户并绑定默认 USER 角色 */
    @Transactional
    public UserInfoResult register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, request.username()));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在：" + request.username());
        }

        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname() == null || request.nickname().isBlank()
                ? request.username() : request.nickname());
        user.setEmail(request.email());
        user.setStatus(1);
        userMapper.insert(user);

        SysRole userRole = roleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, "USER"));
        if (userRole == null) {
            throw new IllegalStateException("默认角色 USER 不存在，请先执行 db/auth-schema.sql");
        }
        SysUserRole relation = new SysUserRole();
        relation.setUserId(user.getId());
        relation.setRoleId(userRole.getId());
        userRoleMapper.insert(relation);

        return toUserInfo(user, List.of("USER"));
    }

    /** 登出：撤销当前访问令牌与传入的刷新令牌 */
    public void logout(RefreshRequest request) {
        tokenService.revoke(SecurityUtils.currentJti(), request.refreshToken());
    }

    /** 刷新令牌换新双令牌（旧刷新令牌作废） */
    public TokenResult refresh(RefreshRequest request) {
        return tokenService.refresh(request.refreshToken());
    }

    /** 当前登录用户信息 */
    public UserInfoResult me() {
        LoginUser loginUser = SecurityUtils.currentUser();
        return toUserInfo(loginUser.getUser(), loginUser.getRoleCodes());
    }

    /** 用户列表（仅管理员），最多返回前 100 条 */
    public List<UserInfoResult> listUsers() {
        Page<SysUser> page = userMapper.selectPage(new Page<>(1, 100), null);
        return page.getRecords().stream()
                .map(user -> toUserInfo(user, roleMapper.selectRoleCodesByUserId(user.getId())))
                .toList();
    }

    private UserInfoResult toUserInfo(SysUser user, List<String> roleCodes) {
        return new UserInfoResult(user.getId(), user.getUsername(), user.getNickname(),
                user.getEmail(), roleCodes);
    }
}