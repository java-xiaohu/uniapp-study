package com.hzy.aillm.model;

import java.util.List;

/**
 * 当前登录用户信息
 *
 * @param roles 角色编码列表，如 ADMIN/USER（不含 ROLE_ 前缀）
 */
public record UserInfoResult(Long id, String username, String nickname, String email, List<String> roles) {
}