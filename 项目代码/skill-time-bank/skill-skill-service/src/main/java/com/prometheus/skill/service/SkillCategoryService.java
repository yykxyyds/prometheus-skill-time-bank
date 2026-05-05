package com.prometheus.skill.service;

import com.prometheus.skill.entity.SkillCategory;

import java.util.List;

/**
 * 技能分类服务接口
 */
public interface SkillCategoryService {

    /**
     * 获取所有分类（按 sortOrder 升序）
     */
    List<SkillCategory> getAllCategories();
}
