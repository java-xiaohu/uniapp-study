package com.hzy.aillm.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 当前登录用户工具：仅在已认证接口内调用
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** 当前登录用户，未登录抛 IllegalStateException */
    public static LoginUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser;
        }
        throw new IllegalStateException("当前无登录用户");
    }

    /** 当前访问令牌的 jti，未登录返回 null */
    public static String currentJti() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof String jti) {
            return jti;
        }
        return null;
    }
}