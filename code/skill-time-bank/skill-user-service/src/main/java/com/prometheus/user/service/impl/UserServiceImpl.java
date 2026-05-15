package com.prometheus.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prometheus.common.BusinessException;
import com.prometheus.common.JwtUtil;
import com.prometheus.common.Result;
import com.prometheus.user.dto.LoginVO;
import com.prometheus.user.dto.UserProfileVO;
import com.prometheus.user.entity.User;
import com.prometheus.user.entity.UserFollow;
import com.prometheus.user.mapper.UserFollowMapper;
import com.prometheus.user.mapper.UserMapper;
import com.prometheus.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    @Transactional
    public void followUser(Long userId, Long targetId) {
        if (userId.equals(targetId)) {
            throw new BusinessException("不能关注自己");
        }
        User target = userMapper.selectById(targetId);
        if (target == null) {
            throw new BusinessException("用户不存在");
        }
        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFollowingId, targetId);
        if (userFollowMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("已关注该用户");
        }
        UserFollow follow = new UserFollow();
        follow.setId(IdUtil.getSnowflakeNextId());
        follow.setFollowerId(userId);
        follow.setFollowingId(targetId);
        follow.setCreateTime(LocalDateTime.now());
        follow.setUpdateTime(LocalDateTime.now());
        userFollowMapper.insert(follow);
        log.info("用户 {} 关注了用户 {}", userId, targetId);
    }

    @Override
    @Transactional
    public void unfollowUser(Long userId, Long targetId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFollowingId, targetId);
        int rows = userFollowMapper.delete(wrapper);
        if (rows == 0) {
            throw new BusinessException("未关注该用户");
        }
        log.info("用户 {} 取消关注用户 {}", userId, targetId);
    }

    @Override
    public Map<String, Object> getFollowStatus(Long userId, Long targetId) {
        Map<String, Object> result = new HashMap<>();
        // 是否已关注
        LambdaQueryWrapper<UserFollow> followingWrapper = new LambdaQueryWrapper<>();
        followingWrapper.eq(UserFollow::getFollowerId, userId)
                .eq(UserFollow::getFollowingId, targetId);
        boolean isFollowing = userFollowMapper.selectCount(followingWrapper) > 0;
        // 是否被对方关注
        LambdaQueryWrapper<UserFollow> followerWrapper = new LambdaQueryWrapper<>();
        followerWrapper.eq(UserFollow::getFollowerId, targetId)
                .eq(UserFollow::getFollowingId, userId);
        boolean isFollowed = userFollowMapper.selectCount(followerWrapper) > 0;
        result.put("isFollowing", isFollowing);
        result.put("isFollowed", isFollowed);
        result.put("isFriend", isFollowing && isFollowed);
        return result;
    }

    @Override
    public List<Map<String, Object>> getFriends(Long userId) {
        // 查到我关注了哪些人
        LambdaQueryWrapper<UserFollow> myFollows = new LambdaQueryWrapper<>();
        myFollows.eq(UserFollow::getFollowerId, userId);
        List<UserFollow> followingList = userFollowMapper.selectList(myFollows);
        if (followingList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> followingIds = followingList.stream()
                .map(UserFollow::getFollowingId).collect(Collectors.toList());
        // 查这些人中哪些也关注了我（互关=好友）
        LambdaQueryWrapper<UserFollow> friendWrapper = new LambdaQueryWrapper<>();
        friendWrapper.eq(UserFollow::getFollowingId, userId)
                .in(UserFollow::getFollowerId, followingIds);
        List<UserFollow> friendFollows = userFollowMapper.selectList(friendWrapper);
        if (friendFollows.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> friendIds = friendFollows.stream()
                .map(UserFollow::getFollowerId).collect(Collectors.toList());
        // 查用户名
        List<User> friends = userMapper.selectBatchIds(friendIds);
        return friends.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("avatar", u.getAvatar());
            m.put("bio", u.getBio());
            return m;
        }).collect(Collectors.toList());
    }
}
