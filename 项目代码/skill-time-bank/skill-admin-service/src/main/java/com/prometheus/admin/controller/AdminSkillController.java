package com.prometheus.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.skill.entity.Skill;
import com.prometheus.skill.mapper.SkillMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/skill")
@RequiredArgsConstructor
public class AdminSkillController {

    private final SkillMapper skillMapper;

    @GetMapping("/list")
    @RequireAuth
    public Result<Page<Skill>> list(HttpServletRequest request,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) Integer status) {
        checkAdmin(request);
        Page<Skill> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Skill::getStatus, status);
        }
        wrapper.orderByDesc(Skill::getCreateTime);
        return Result.success(skillMapper.selectPage(pageParam, wrapper));
    }

    @PutMapping("/{id}/status")
    @RequireAuth
    public Result<Void> updateStatus(HttpServletRequest request,
                                     @PathVariable Long id,
                                     @RequestBody Map<String, Object> body) {
        checkAdmin(request);
        Integer status = (Integer) body.get("status");
        if (status == null || (status != 1 && status != 3)) {
            throw new BusinessException(400, "状态值无效（1=通过，3=拒绝）");
        }
        Skill skill = skillMapper.selectById(id);
        if (skill == null) {
            throw new BusinessException(404, "技能不存在");
        }
        skill.setStatus(status);
        skill.setUpdateTime(LocalDateTime.now());
        skillMapper.updateById(skill);
        log.info("管理员审核技能 {}: status={}", id, status);
        return Result.success();
    }

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            throw new BusinessException(403, "无管理员权限");
        }
    }
}
