package com.prometheus.user.service;

import com.prometheus.common.Result;

public interface UserService {

    /**
     * 用户注册
     */
    Result<?> register(String username, String password, String email);

    /**
     * 用户登录
     */
    Result<?> login(String username, String password);

    /**
     * 获取个人资料（含技能数、关注数，排除密码）
     */
    Result<?> getProfile(Long userId);

    /**
     * 更新个人资料（只能改自己的）
     */
    Result<?> updateProfile(Long userId, String email, String phone, String bio, String avatar);
}
