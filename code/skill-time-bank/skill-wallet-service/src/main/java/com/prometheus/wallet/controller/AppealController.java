package com.prometheus.wallet.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.wallet.entity.Appeal;
import com.prometheus.wallet.service.AppealService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/appeal")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;

    /** 提交申诉 */
    @PostMapping
    @RequireAuth
    public Result<Void> create(HttpServletRequest request, @RequestBody Appeal appeal) {
        Long userId = (Long) request.getAttribute("userId");
        appeal.setUserId(userId);
        appealService.createAppeal(appeal);
        return Result.success();
    }

    /** 申诉列表（仅管理员） */
    @GetMapping("/list")
    @RequireAuth
    public Result<Page<Appeal>> list(HttpServletRequest request,
                                      @RequestParam(name = "page", defaultValue = "1") int page,
                                      @RequestParam(name = "size", defaultValue = "10") int size,
                                      @RequestParam(name = "status", required = false) Integer status) {
        checkAdmin(request);
        return Result.success(appealService.getAppeals(page, size, status));
    }

    /** 处理申诉（仅管理员） */
    @PutMapping("/{id}/handle")
    @RequireAuth
    public Result<Void> handle(HttpServletRequest request,
                                @PathVariable Long id,
                                @RequestBody Map<String, String> body) {
        checkAdmin(request);
        Long adminId = (Long) request.getAttribute("userId");
        String result = body.get("result");
        if (result == null || result.isBlank()) {
            throw new BusinessException(400, "处理结果不能为空");
        }
        String decision = body.get("decision");
        if (decision == null || (!"ACCEPT_REFUND".equals(decision) && !"ACCEPT_COMPLETE".equals(decision) && !"REJECT".equals(decision))) {
            decision = "ACCEPT_REFUND"; // 兼容旧调用，默认退款
        }
        appealService.handleAppeal(id, result, adminId, decision);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无管理员权限");
        }
    }
}
