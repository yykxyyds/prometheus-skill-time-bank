package com.prometheus.user.controller;

import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.user.dto.LoginRequest;
import com.prometheus.user.dto.RegisterRequest;
import com.prometheus.user.dto.UpdateProfileRequest;
import com.prometheus.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterRequest req) {
        return userService.register(req.getUsername(), req.getPassword(), req.getEmail());
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginRequest req) {
        return userService.login(req.getUsername(), req.getPassword());
    }

    /**
     * 查看自己的个人资料（需登录，从 token 解析 userId）
     */
    @GetMapping("/profile")
    @RequireAuth
    public Result<?> getOwnProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getProfile(userId);
    }

    /**
     * 更新个人资料（需登录，只能改自己的）
     */
    @PutMapping("/profile")
    @RequireAuth
    public Result<?> updateProfile(@RequestBody UpdateProfileRequest req,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.updateProfile(userId, req.getEmail(), req.getPhone(),
                req.getBio(), req.getAvatar());
    }

    /**
     * 查看他人主页（无需登录）
     */
    @GetMapping("/{userId}/profile")
    public Result<?> getOtherProfile(@PathVariable Long userId) {
        return userService.getProfile(userId);
    }

    // ========== 关注/好友 ==========

    /**
     * 关注用户（需登录）
     */
    @RequireAuth
    @PostMapping("/follow/{targetId}")
    public Result<Void> follow(@PathVariable Long targetId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.followUser(userId, targetId);
        return Result.success();
    }

    /**
     * 取消关注（需登录）
     */
    @RequireAuth
    @DeleteMapping("/follow/{targetId}")
    public Result<Void> unfollow(@PathVariable Long targetId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userService.unfollowUser(userId, targetId);
        return Result.success();
    }

    /**
     * 查询关注状态（需登录）
     */
    @RequireAuth
    @GetMapping("/follow/{targetId}/status")
    public Result<Map<String, Object>> followStatus(@PathVariable Long targetId,
                                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getFollowStatus(userId, targetId));
    }

    /**
     * 好友列表（互相关注，需登录）
     */
    @RequireAuth
    @GetMapping("/friends")
    public Result<List<Map<String, Object>>> friends(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getFriends(userId));
    }
}
