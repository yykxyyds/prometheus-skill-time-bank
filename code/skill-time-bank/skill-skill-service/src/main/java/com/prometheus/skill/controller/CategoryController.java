package com.prometheus.skill.controller;

import com.prometheus.common.Result;
import com.prometheus.skill.entity.SkillCategory;
import com.prometheus.skill.service.SkillCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 技能分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final SkillCategoryService categoryService;

    /**
     * 获取所有分类（无需登录）
     */
    @GetMapping("/list")
    public Result<List<SkillCategory>> list() {
        List<SkillCategory> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
}
