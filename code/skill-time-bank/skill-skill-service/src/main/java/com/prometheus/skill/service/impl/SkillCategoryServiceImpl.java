package com.prometheus.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.prometheus.skill.entity.SkillCategory;
import com.prometheus.skill.mapper.SkillCategoryMapper;
import com.prometheus.skill.service.SkillCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 技能分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillCategoryServiceImpl implements SkillCategoryService {

    private final SkillCategoryMapper categoryMapper;

    @Override
    public List<SkillCategory> getAllCategories() {
        LambdaQueryWrapper<SkillCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SkillCategory::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }
}
