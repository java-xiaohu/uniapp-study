package com.hzy.aillm.model;

import jakarta.validation.constraints.NotBlank;

/**
 * 刷新令牌 / 登出入参（访问令牌从 Authorization 头获取）
 */
public record RefreshRequest(
        @NotBlank(message = "refreshToken 不能为空") String refreshToken) {
}