package com.prometheus.skill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.skill.entity.Skill;

/**
 * 技能服务接口
 */
public interface SkillService {

    /**
     * 技能广场列表（分页+筛选+排序）
     */
    Page<Skill> getSkillList(int page, int size, Long categoryId, String keyword, String sort);

    /**
     * 技能详情（同时增加浏览量）
     */
    Skill getSkillDetail(Long id);

    /**
     * 发布技能
     */
    void publishSkill(Skill skill);

    /**
     * 更新技能（只能更新自己的）
     */
    void updateSkill(Skill skill);

    /**
     * 下架技能（只能下架自己的）
     */
    void offlineSkill(Long skillId, Long userId);

    /**
     * 浏览量+1
     */
    void incrementViewCount(Long id);

    /**
     * 我的技能列表
     */
    Page<Skill> getMySkillList(int page, int size, Long userId);
}
