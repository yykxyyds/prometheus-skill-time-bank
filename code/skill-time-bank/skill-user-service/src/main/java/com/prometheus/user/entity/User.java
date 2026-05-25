package com.prometheus.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`user`")
public class User extends BaseEntity {

    private String username;

    @JsonIgnore
    private String password;

    private String email;

    private String phone;

    private String avatar;

    private String role;

    private Integer status;

    private Integer balance;

    @TableField("frozen_balance")
    private Integer frozenBalance;

    private String bio;

    @Version
    private Integer version;
}
