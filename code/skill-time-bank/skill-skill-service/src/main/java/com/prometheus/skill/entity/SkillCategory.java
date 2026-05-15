package com.prometheus.skill.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.prometheus.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 技能分类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("skill_category")
public class SkillCategory extends BaseEntity {

    /** 分类名称（唯一） */
    private String name;

    /** 分类图标URL */
    private String icon;

    /** 排序权重（越小越靠前） */
    private Integer sortOrder;
}
