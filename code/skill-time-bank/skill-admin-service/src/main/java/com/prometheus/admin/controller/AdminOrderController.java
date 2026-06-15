package com.prometheus.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.order.entity.SkillOrder;
import com.prometheus.order.mapper.SkillOrderMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final SkillOrderMapper skillOrderMapper;

    @GetMapping("/list")
    @RequireAuth
    public Result<Page<SkillOrder>> list(HttpServletRequest request,
                                          @RequestParam(name = "page", defaultValue = "1") int page,
                                          @RequestParam(name = "size", defaultValue = "10") int size,
                                          @RequestParam(name = "status", required = false) Integer status) {
        checkAdmin(request);

        Page<SkillOrder> pageResult = skillOrderMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<SkillOrder>()
                        .eq(status != null, SkillOrder::getStatus, status)
                        .orderByDesc(SkillOrder::getCreateTime));

        List<Long> ids = pageResult.getRecords().stream().map(SkillOrder::getId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            Map<Long, SkillOrder> details = skillOrderMapper.selectBatchIdsWithDetails(ids).stream()
                    .collect(Collectors.toMap(SkillOrder::getId, Function.identity()));
            for (int i = 0; i < pageResult.getRecords().size(); i++) {
                SkillOrder enriched = details.get(pageResult.getRecords().get(i).getId());
                if (enriched != null) {
                    pageResult.getRecords().set(i, enriched);
                }
            }
        }

        return Result.success(pageResult);
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无管理员权限");
        }
    }
}
