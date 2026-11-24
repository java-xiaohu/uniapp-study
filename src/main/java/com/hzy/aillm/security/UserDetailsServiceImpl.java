package com.hzy.aillm.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hzy.aillm.entity.SysUser;
import com.hzy.aillm.mapper.SysRoleMapper;
import com.hzy.aillm.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 按用户名加载用户与角色，供密码认证与 JWT 过滤器复用
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }
        return new LoginUser(user, roleMapper.selectRoleCodesByUserId(user.getId()));
    }
}