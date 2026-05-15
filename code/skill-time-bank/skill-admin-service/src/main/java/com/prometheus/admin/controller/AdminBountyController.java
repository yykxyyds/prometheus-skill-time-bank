package com.prometheus.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.skill.entity.Bounty;
import com.prometheus.skill.mapper.BountyMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/bounty")
@RequiredArgsConstructor
public class AdminBountyController {

    private final BountyMapper bountyMapper;

    @GetMapping("/list")
    @RequireAuth
    public Result<Page<Bounty>> list(HttpServletRequest request,
                                     @RequestParam(name = "page", defaultValue = "1") int page,
                                     @RequestParam(name = "size", defaultValue = "10") int size,
                                     @RequestParam(name = "status", required = false) Integer status) {
        checkAdmin(request);
        Page<Bounty> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Bounty> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Bounty::getStatus, status);
        }
        wrapper.orderByDesc(Bounty::getCreateTime);
        return Result.success(bountyMapper.selectPage(pageParam, wrapper));
    }

    @PutMapping("/{id}/status")
    @RequireAuth
    public Result<Void> updateStatus(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @RequestBody Map<String, Object> body) {
        checkAdmin(request);
        Integer status = (Integer) body.get("status");
        if (status == null || (status != 1 && status != 4)) {
            throw new BusinessException(400, "状态值无效（1=通过，4=拒绝）");
        }
        Bounty bounty = bountyMapper.selectById(id);
        if (bounty == null) {
            throw new BusinessException(404, "悬赏不存在");
        }
        bounty.setStatus(status);
        bounty.setUpdateTime(LocalDateTime.now());
        bountyMapper.updateById(bounty);
        log.info("管理员审核悬赏 {}: status={}", id, status);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无管理员权限");
        }
    }
}
