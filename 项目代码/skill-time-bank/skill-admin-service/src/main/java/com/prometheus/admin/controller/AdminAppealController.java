package com.prometheus.admin.controller;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/appeal")
@RequiredArgsConstructor
public class AdminAppealController {

    private final AppealService appealService;

    /** 申诉列表 */
    @GetMapping("/list")
    @RequireAuth
    public Result<Page<Appeal>> list(HttpServletRequest request,
                                      @RequestParam(name = "page", defaultValue = "1") int page,
                                      @RequestParam(name = "size", defaultValue = "10") int size,
                                      @RequestParam(name = "status", required = false) Integer status) {
        checkAdmin(request);
        return Result.success(appealService.getAppeals(page, size, status));
    }

    /** 处理申诉 */
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
        appealService.handleAppeal(id, result, adminId);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无管理员权限");
        }
    }
}
