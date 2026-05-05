package com.prometheus.wallet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.wallet.entity.Announcement;
import com.prometheus.wallet.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /** 公告列表（无需登录） */
    @GetMapping("/list")
    public Result<Page<Announcement>> list(@RequestParam(name = "page", defaultValue = "1") int page,
                                           @RequestParam(name = "size", defaultValue = "10") int size) {
        return Result.success(announcementService.getAnnouncements(page, size));
    }

    /** 公告详情 */
    @GetMapping("/{id}")
    public Result<Announcement> detail(@PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementDetail(id));
    }

    /** 发布公告（仅管理员） */
    @PostMapping
    @RequireAuth
    public Result<Void> create(HttpServletRequest request, @RequestBody Announcement announcement) {
        checkAdmin(request);
        Long userId = (Long) request.getAttribute("userId");
        announcementService.createAnnouncement(announcement, userId);
        return Result.success();
    }

    /** 更新公告（仅管理员） */
    @PutMapping
    @RequireAuth
    public Result<Void> update(HttpServletRequest request, @RequestBody Announcement announcement) {
        checkAdmin(request);
        announcementService.updateAnnouncement(announcement);
        return Result.success();
    }

    /** 删除公告（仅管理员） */
    @DeleteMapping("/{id}")
    @RequireAuth
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        checkAdmin(request);
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无管理员权限");
        }
    }
}
