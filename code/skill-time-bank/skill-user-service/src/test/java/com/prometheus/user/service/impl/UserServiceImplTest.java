package com.prometheus.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prometheus.common.BusinessException;
import com.prometheus.common.Result;
import com.prometheus.user.dto.LoginVO;
import com.prometheus.user.dto.UserProfileVO;
import com.prometheus.user.entity.User;
import com.prometheus.user.entity.UserFollow;
import com.prometheus.user.mapper.UserFollowMapper;
import com.prometheus.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserFollowMapper userFollowMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        // simulate MyBatis-Plus ASSIGN_ID: set id when insert is called
        doAnswer(inv -> {
            User u = inv.getArgument(0);
            if (u.getId() == null) {
                u.setId(1L);
            }
            return null;
        }).when(userMapper).insert(any(User.class));

        doAnswer(inv -> {
            UserFollow f = inv.getArgument(0);
            if (f.getId() == null) {
                f.setId(100L);
            }
            return null;
        }).when(userFollowMapper).insert(any(UserFollow.class));
    }

    // ==================== register ====================

    @Test
    void testRegister_Success() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        Result<?> result = userService.register("newuser", "password123", "new@test.com");

        assertNotNull(result);
        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertTrue(result.getData() instanceof LoginVO);

        LoginVO vo = (LoginVO) result.getData();
        assertEquals(1L, vo.getUserId());
        assertEquals("newuser", vo.getUsername());
        assertEquals("USER", vo.getRole());
        assertEquals(100, vo.getBalance());
        assertNotNull(vo.getToken());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void testRegister_EmptyUsername_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.register("", "password123", "a@test.com"));
        assertTrue(ex.getMessage().contains("用户名不能为空"));
        verify(userMapper, never()).insert(any());
    }

    @Test
    void testRegister_NullUsername_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.register(null, "password123", "a@test.com"));
        assertTrue(ex.getMessage().contains("用户名不能为空"));
    }

    @Test
    void testRegister_ShortPassword_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.register("user", "12345", "a@test.com"));
        assertTrue(ex.getMessage().contains("不能少于6位"));
    }

    @Test
    void testRegister_NullPassword_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.register("user", null, "a@test.com"));
        assertTrue(ex.getMessage().contains("不能少于6位"));
    }

    @Test
    void testRegister_DuplicateUsername_Throws() {
        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.register("existing", "password123", "a@test.com"));
        assertTrue(ex.getMessage().contains("用户名已存在"));
        verify(userMapper, never()).insert(any());
    }

    // ==================== login ====================

    @Test
    void testLogin_Success() {
        String rawPassword = "password123";
        User mockUser = buildUser(2L, "testuser", rawPassword, 1, "USER", 200);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        Result<?> result = userService.login("testuser", rawPassword);

        assertNotNull(result);
        assertEquals(200, result.getCode());
        LoginVO vo = (LoginVO) result.getData();
        assertEquals(2L, vo.getUserId());
        assertEquals("testuser", vo.getUsername());
        assertEquals("USER", vo.getRole());
        assertNotNull(vo.getToken());
    }

    @Test
    void testLogin_EmptyUsername_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.login("", "password"));
        assertTrue(ex.getMessage().contains("用户名不能为空"));
    }

    @Test
    void testLogin_EmptyPassword_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.login("user", ""));
        assertTrue(ex.getMessage().contains("密码不能为空"));
    }

    @Test
    void testLogin_UserNotFound_Throws() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.login("nouser", "password123"));
        assertTrue(ex.getMessage().contains("用户名或密码错误"));
    }

    @Test
    void testLogin_WrongPassword_Throws() {
        User mockUser = buildUser(3L, "user", "correctPassword", 1, "USER", 100);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.login("user", "wrongPassword"));
        assertTrue(ex.getMessage().contains("用户名或密码错误"));
    }

    @Test
    void testLogin_DisabledAccount_Throws() {
        User mockUser = buildUser(4L, "disabled", "password123", 0, "USER", 100);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.login("disabled", "password123"));
        assertTrue(ex.getMessage().contains("已被禁用"));
    }

    @Test
    void testLogin_NullRole_TreatedAsUSER() {
        User mockUser = buildUser(5L, "norole", "password123", 1, null, 50);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(mockUser);

        Result<?> result = userService.login("norole", "password123");
        LoginVO vo = (LoginVO) result.getData();
        assertEquals("USER", vo.getRole());
    }

    // ==================== getProfile ====================

    @Test
    void testGetProfile_Success() {
        User mockUser = buildUser(1L, "profileuser", "pass", 1, "USER", 100);
        mockUser.setBio("Hello");
        when(userMapper.selectById(1L)).thenReturn(mockUser);
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(3L, 5L);

        Result<?> result = userService.getProfile(1L);
        assertEquals(200, result.getCode());

        UserProfileVO vo = (UserProfileVO) result.getData();
        assertEquals(1L, vo.getId());
        assertEquals("profileuser", vo.getUsername());
        assertEquals("Hello", vo.getBio());
        assertEquals(3L, vo.getFollowerCount());
        assertEquals(5L, vo.getFollowingCount());
    }

    @Test
    void testGetProfile_NotFound_Throws() {
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.getProfile(999L));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    // ==================== updateProfile ====================

    @Test
    void testUpdateProfile_Success() {
        User mockUser = buildUser(1L, "user", "pass", 1, "USER", 100);
        when(userMapper.selectById(1L)).thenReturn(mockUser);

        Result<?> result = userService.updateProfile(1L, "new@email.com", "13800001111", "New bio", "/avatar.png");
        assertEquals(200, result.getCode());
        verify(userMapper).updateById(mockUser);
    }

    @Test
    void testUpdateProfile_NotFound_Throws() {
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.updateProfile(999L, "email", "phone", "bio", "avatar"));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    // ==================== followUser ====================

    @Test
    void testFollowUser_Success() {
        User target = buildUser(2L, "target", "pass", 1, "USER", 100);
        when(userMapper.selectById(2L)).thenReturn(target);
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        userService.followUser(1L, 2L);

        verify(userFollowMapper).insert(any(UserFollow.class));
    }

    @Test
    void testFollowUser_Self_Throws() {
        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.followUser(1L, 1L));
        assertTrue(ex.getMessage().contains("不能关注自己"));
    }

    @Test
    void testFollowUser_TargetNotFound_Throws() {
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.followUser(1L, 999L));
        assertTrue(ex.getMessage().contains("用户不存在"));
    }

    @Test
    void testFollowUser_Duplicate_Throws() {
        User target = buildUser(2L, "target", "pass", 1, "USER", 100);
        when(userMapper.selectById(2L)).thenReturn(target);
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.followUser(1L, 2L));
        assertTrue(ex.getMessage().contains("已关注"));
        verify(userFollowMapper, never()).insert(any());
    }

    // ==================== unfollowUser ====================

    @Test
    void testUnfollowUser_Success() {
        when(userFollowMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(1);

        userService.unfollowUser(1L, 2L);

        verify(userFollowMapper).delete(any(LambdaQueryWrapper.class));
    }

    @Test
    void testUnfollowUser_NotFollowing_Throws() {
        when(userFollowMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                userService.unfollowUser(1L, 2L));
        assertTrue(ex.getMessage().contains("未关注"));
    }

    // ==================== getFollowStatus ====================

    @Test
    void testGetFollowStatus_FollowingAndFollowed() {
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(1L, 1L);

        Map<String, Object> result = userService.getFollowStatus(1L, 2L);

        assertTrue((Boolean) result.get("isFollowing"));
        assertTrue((Boolean) result.get("isFollowed"));
        assertTrue((Boolean) result.get("isFriend"));
    }

    @Test
    void testGetFollowStatus_NotFollowing() {
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L, 1L);

        Map<String, Object> result = userService.getFollowStatus(1L, 2L);

        assertFalse((Boolean) result.get("isFollowing"));
        assertTrue((Boolean) result.get("isFollowed"));
        assertFalse((Boolean) result.get("isFriend"));
    }

    @Test
    void testGetFollowStatus_NeitherFollowed() {
        when(userFollowMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L, 0L);

        Map<String, Object> result = userService.getFollowStatus(1L, 2L);

        assertFalse((Boolean) result.get("isFollowing"));
        assertFalse((Boolean) result.get("isFollowed"));
        assertFalse((Boolean) result.get("isFriend"));
    }

    // ==================== getFriends ====================

    @Test
    void testGetFriends_NoFollowing() {
        when(userFollowMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        List<Map<String, Object>> result = userService.getFriends(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetFriends_HasMutualFriends() {
        UserFollow myFollow = new UserFollow();
        myFollow.setFollowerId(1L);
        myFollow.setFollowingId(2L);

        UserFollow theirFollow = new UserFollow();
        theirFollow.setFollowerId(2L);
        theirFollow.setFollowingId(1L);

        User friend = buildUser(2L, "friend", "pass", 1, "USER", 100);
        friend.setAvatar("/avatar.png");
        friend.setBio("Friendly");

        when(userFollowMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(myFollow))       // my follows
                .thenReturn(List.of(theirFollow));    // mutual check
        when(userMapper.selectBatchIds(anyList())).thenReturn(List.of(friend));

        List<Map<String, Object>> result = userService.getFriends(1L);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).get("id"));
        assertEquals("friend", result.get(0).get("username"));
    }

    @Test
    void testGetFriends_NoMutual() {
        UserFollow myFollow = new UserFollow();
        myFollow.setFollowerId(1L);
        myFollow.setFollowingId(2L);

        when(userFollowMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(myFollow))          // my follows
                .thenReturn(Collections.emptyList());   // no mutual

        List<Map<String, Object>> result = userService.getFriends(1L);
        assertTrue(result.isEmpty());
    }

    // ==================== helpers ====================

    private User buildUser(Long id, String username, String rawPassword, int status, String role, int balance) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(encoder.encode(rawPassword));
        user.setStatus(status);
        user.setRole(role);
        user.setBalance(balance);
        user.setFrozenBalance(0);
        return user;
    }
}
