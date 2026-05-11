package com.prometheus.user.service;

import com.prometheus.common.Result;

import java.util.List;
import java.util.Map;

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

    /**
     * 关注用户
     */
    void followUser(Long userId, Long targetId);

    /**
     * 取消关注
     */
    void unfollowUser(Long userId, Long targetId);

    /**
     * 查询关注状态
     */
    Map<String, Object> getFollowStatus(Long userId, Long targetId);

    /**
     * 好友列表（互相关注）
     */
    List<Map<String, Object>> getFriends(Long userId);
}
