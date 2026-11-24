package com.hzy.aillm.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册入参
 */
public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 50, message = "用户名长度需在 3-50 之间")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名仅允许字母、数字、下划线和连字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在 6-64 之间")
        String password,

        @Size(max = 50, message = "昵称最长 50 个字符")
        String nickname,

        @Email(message = "邮箱格式不正确")
        String email) {
}