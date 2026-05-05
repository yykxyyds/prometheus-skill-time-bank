package com.prometheus.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String avatar;
    private String role;
    private Integer status;
    private Integer balance;
    private Integer frozenBalance;
    private String bio;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long followerCount;
    private Long followingCount;
    private Long skillCount;
}
