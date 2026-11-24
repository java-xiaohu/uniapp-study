package com.hzy.aillm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户
 */
@Data
@TableName("sys_user")
public class SysUser {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名，唯一 */
    private String username;

    /** BCrypt 密码哈希 */
    private String password;

    private String nickname;

    private String email;

    /** 状态：1启用 0禁用 */
    private Integer status;

    /** 逻辑删除：0未删 1已删 */
    @TableLogic
    private Integer deleted;

    /** 由数据库默认值维护 */
    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}