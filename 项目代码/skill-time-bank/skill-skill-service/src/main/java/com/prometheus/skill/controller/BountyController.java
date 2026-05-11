package com.prometheus.skill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.skill.entity.Bounty;
import com.prometheus.skill.service.BountyService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 悬赏控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/bounty")
@RequiredArgsConstructor
public class BountyController {

    private final BountyService bountyService;
    private final HttpServletRequest request;

    /**
     * 悬赏列表（无需登录，但按用户筛选时需登录）
     *
     * @param type 筛选类型：null-全部, "publish"-我发布的, "take"-我接的, "complete"-我完成的
     */
    @GetMapping("/list")
    public Result<Page<Bounty>> list(@RequestParam(name = "page", defaultValue = "1") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size,
                                     @RequestParam(name = "status", required = false) Integer status,
                                     @RequestParam(name = "type", required = false) String type) {
        Long userId = type != null ? getCurrentUserId() : null;
        Page<Bounty> result = bountyService.getBountyList(page, size, status, type, userId);
        return Result.success(result);
    }

    /**
     * 悬赏详情（无需登录）
     */
    @GetMapping("/{id}")
    public Result<Bounty> detail(@PathVariable Long id) {
        return Result.success(bountyService.getBountyDetail(id));
    }

    /**
     * 获取悬赏的申请列表（需登录，仅悬赏发布者可查看）
     */
    @RequireAuth
    @GetMapping("/{id}/applications")
    public Result<List<com.prometheus.skill.entity.BountyApplication>> applications(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        return Result.success(bountyService.getApplications(id, userId));
    }

    /**
     * 发布悬赏（需登录）
     */
    @RequireAuth
    @PostMapping
    public Result<String> publish(@RequestBody Bounty bounty) {
        Long userId = getCurrentUserId();
        bounty.setUserId(userId);
        bountyService.publishBounty(bounty);
        return Result.success("发布成功");
    }

    /**
     * 申请接悬赏（需登录）
     */
    @RequireAuth
    @PostMapping("/{id}/apply")
    public Result<String> apply(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String message = body.getOrDefault("message", "");
        bountyService.applyBounty(id, userId, message);
        return Result.success("申请成功");
    }

    /**
     * 接受申请（需登录，仅悬赏发布者）
     */
    @RequireAuth
    @PutMapping("/{id}/accept/{applicationId}")
    public Result<String> accept(@PathVariable Long id, @PathVariable Long applicationId) {
        Long userId = getCurrentUserId();
        bountyService.acceptApplication(id, applicationId, userId);
        return Result.success("已接受");
    }

    /**
     * 拒绝申请（需登录，仅悬赏发布者）
     */
    @RequireAuth
    @PutMapping("/{id}/reject/{applicationId}")
    public Result<String> reject(@PathVariable Long id, @PathVariable Long applicationId) {
        Long userId = getCurrentUserId();
        bountyService.rejectApplication(id, applicationId, userId);
        return Result.success("已拒绝");
    }

    /**
     * 确认完成悬赏（需登录，仅悬赏发布者）
     */
    @RequireAuth
    @PutMapping("/{id}/complete")
    public Result<String> complete(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        bountyService.completeBounty(id, userId);
        return Result.success("悬赏已完成");
    }

    /**
     * 从请求属性获取当前用户ID（由登录拦截器设置）
     */
    private Long getCurrentUserId() {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new com.prometheus.common.BusinessException(401, "请先登录");
        }
        return userId;
    }
}
