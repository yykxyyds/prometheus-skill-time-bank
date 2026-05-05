package com.prometheus.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prometheus.common.BusinessException;
import com.prometheus.common.JwtUtil;
import com.prometheus.common.Result;
import com.prometheus.user.dto.LoginVO;
import com.prometheus.user.dto.UserProfileVO;
import com.prometheus.user.entity.User;
import com.prometheus.user.mapper.UserFollowMapper;
import com.prometheus.user.mapper.UserMapper;
import com.prometheus.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserFollowMapper userFollowMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result<?> register(String username, String password, String email) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new BusinessException("密码长度不能少于6位");
        }

        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole("USER");
        user.setStatus(1);
        user.setBalance(100);  // 初始赠送100时间币
        user.setFrozenBalance(0);
        userMapper.insert(user);

        // 生成 token
        String token = JwtUtil.generateToken(user.getId(), username, "USER");
        log.info("用户注册成功: username={}, userId={}", username, user.getId());

        LoginVO vo = new LoginVO(token, user.getId(), username, "USER", user.getBalance());
        return Result.success(vo);
    }

    @Override
    public Result<?> login(String username, String password) {
        if (username == null || username.isBlank()) {
            throw new BusinessException("用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException("密码不能为空");
        }

        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查账户状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }

        // 生成 token
        String token = JwtUtil.generateToken(user.getId(), username,
                user.getRole() != null ? user.getRole() : "USER");
        log.info("用户登录成功: username={}, userId={}", username, user.getId());

        LoginVO vo = new LoginVO(token, user.getId(), username,
                user.getRole() != null ? user.getRole() : "USER", user.getBalance());
        return Result.success(vo);
    }

    @Override
    public Result<?> getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 查询关注统计
        LambdaQueryWrapper<com.prometheus.user.entity.UserFollow> followerWrapper = new LambdaQueryWrapper<>();
        followerWrapper.eq(com.prometheus.user.entity.UserFollow::getFollowingId, userId);
        Long followerCount = userFollowMapper.selectCount(followerWrapper);

        LambdaQueryWrapper<com.prometheus.user.entity.UserFollow> followingWrapper = new LambdaQueryWrapper<>();
        followingWrapper.eq(com.prometheus.user.entity.UserFollow::getFollowerId, userId);
        Long followingCount = userFollowMapper.selectCount(followingWrapper);

        // 构造返回 (排除密码，User 实体上 password 已加 @JsonIgnore)
        UserProfileVO vo = new UserProfileVO();
        BeanUtil.copyProperties(user, vo);
        vo.setFollowerCount(followerCount);
        vo.setFollowingCount(followingCount);
        vo.setSkillCount(0L); // 技能数由 skill-service 跨模块查询，暂为0

        return Result.success(vo);
    }

    @Override
    public Result<?> updateProfile(Long userId, String email, String phone, String bio, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (email != null) {
            user.setEmail(email);
        }
        if (phone != null) {
            user.setPhone(phone);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }

        userMapper.updateById(user);
        log.info("用户资料更新: userId={}", userId);
        return Result.success("更新成功");
    }
}
