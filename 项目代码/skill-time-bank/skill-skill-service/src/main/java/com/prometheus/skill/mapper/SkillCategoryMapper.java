package com.prometheus.skill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prometheus.skill.entity.SkillCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 技能分类 Mapper
 */
@Mapper
public interface SkillCategoryMapper extends BaseMapper<SkillCategory> {
}
