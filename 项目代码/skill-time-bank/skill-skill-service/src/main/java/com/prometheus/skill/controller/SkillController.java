package com.prometheus.skill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.Result;
import com.prometheus.common.annotation.RequireAuth;
import com.prometheus.skill.entity.Skill;
import com.prometheus.skill.service.SkillService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 技能控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/skill")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final HttpServletRequest request;

    /**
     * 技能广场列表（无需登录）
     */
    @GetMapping("/list")
    public Result<Page<Skill>> list(@RequestParam(name = "page", defaultValue = "1") int page,
                                    @RequestParam(name = "size", defaultValue = "10") int size,
                                    @RequestParam(name = "categoryId", required = false) Long categoryId,
                                    @RequestParam(name = "keyword", required = false) String keyword,
                                    @RequestParam(name = "sort", defaultValue = "latest") String sort) {
        Page<Skill> result = skillService.getSkillList(page, size, categoryId, keyword, sort);
        return Result.success(result);
    }

    /**
     * 技能详情
     */
    @GetMapping("/{id}")
    public Result<Skill> detail(@PathVariable Long id) {
        Skill skill = skillService.getSkillDetail(id);
        return Result.success(skill);
    }

    /**
     * 发布技能（需登录）
     */
    @RequireAuth
    @PostMapping
    public Result<String> publish(@RequestBody Skill skill) {
        Long userId = getCurrentUserId();
        skill.setUserId(userId);
        skillService.publishSkill(skill);
        return Result.success("发布成功");
    }

    /**
     * 更新技能（需登录）
     */
    @RequireAuth
    @PutMapping
    public Result<String> update(@RequestBody Skill skill) {
        Long userId = getCurrentUserId();
        skill.setUserId(userId);
        skillService.updateSkill(skill);
        return Result.success("更新成功");
    }

    /**
     * 下架技能（需登录）
     */
    @RequireAuth
    @PutMapping("/{id}/offline")
    public Result<String> offline(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        skillService.offlineSkill(id, userId);
        return Result.success("下架成功");
    }

    /**
     * 我的技能列表（需登录）
     */
    @RequireAuth
    @GetMapping("/my")
    public Result<Page<Skill>> mySkills(@RequestParam(name = "page", defaultValue = "1") int page,
                                        @RequestParam(name = "size", defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        Page<Skill> result = skillService.getMySkillList(page, size, userId);
        return Result.success(result);
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
