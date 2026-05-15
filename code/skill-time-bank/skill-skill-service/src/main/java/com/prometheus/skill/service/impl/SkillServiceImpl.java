package com.prometheus.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.skill.entity.Skill;
import com.prometheus.skill.mapper.SkillMapper;
import com.prometheus.skill.service.SkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 技能服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillMapper skillMapper;

    @Override
    public Page<Skill> getSkillList(int page, int size, Long categoryId, String keyword, String sort) {
        Page<Skill> pageParam = new Page<>(page, size);
        return skillMapper.selectPageWithUser(pageParam, categoryId, keyword, sort);
    }

    @Override
    public Skill getSkillDetail(Long id) {
        Skill skill = skillMapper.selectByIdWithUser(id);
        if (skill == null) {
            throw new BusinessException("技能不存在");
        }
        incrementViewCount(id);
        skill.setViewCount(skill.getViewCount() + 1);
        return skill;
    }

    @Override
    public void publishSkill(Skill skill) {
        if (skill.getTitle() == null || skill.getTitle().trim().isEmpty()) {
            throw new BusinessException("技能标题不能为空");
        }
        if (skill.getCategoryId() == null) {
            throw new BusinessException("请选择技能分类");
        }
        if (skill.getPrice() == null || skill.getPrice() <= 0) {
            throw new BusinessException("价格必须大于0");
        }
        skill.setStatus(2); // 待审核
        skill.setViewCount(0);
        skill.setOrderCount(0);
        skill.setCreateTime(LocalDateTime.now());
        skill.setUpdateTime(LocalDateTime.now());
        skillMapper.insert(skill);
        log.info("用户 {} 发布了技能: {}", skill.getUserId(), skill.getTitle());
    }

    @Override
    public void updateSkill(Skill skill) {
        Skill existing = skillMapper.selectById(skill.getId());
        if (existing == null) {
            throw new BusinessException("技能不存在");
        }
        if (!existing.getUserId().equals(skill.getUserId())) {
            throw new BusinessException("只能修改自己的技能");
        }
        // 只允许更新部分字段
        existing.setTitle(skill.getTitle());
        existing.setDescription(skill.getDescription());
        existing.setPrice(skill.getPrice());
        existing.setAvailableTime(skill.getAvailableTime());
        existing.setCoverImage(skill.getCoverImage());
        existing.setCategoryId(skill.getCategoryId());
        existing.setStatus(2); // 修改后重新待审核
        existing.setUpdateTime(LocalDateTime.now());
        skillMapper.updateById(existing);
        log.info("技能 {} 已更新", skill.getId());
    }

    @Override
    public void offlineSkill(Long skillId, Long userId) {
        Skill skill = skillMapper.selectById(skillId);
        if (skill == null) {
            throw new BusinessException("技能不存在");
        }
        if (!skill.getUserId().equals(userId)) {
            throw new BusinessException("只能下架自己的技能");
        }
        if (skill.getStatus() == 0) {
            throw new BusinessException("技能已下架");
        }
        skill.setStatus(0);
        skill.setUpdateTime(LocalDateTime.now());
        skillMapper.updateById(skill);
        log.info("用户 {} 下架了技能 {}", userId, skillId);
    }

    @Override
    public void incrementViewCount(Long id) {
        LambdaUpdateWrapper<Skill> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Skill::getId, id)
                .setSql("view_count = view_count + 1");
        skillMapper.update(null, wrapper);
    }

    @Override
    public Page<Skill> getMySkillList(int page, int size, Long userId) {
        Page<Skill> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Skill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Skill::getUserId, userId)
                .orderByDesc(Skill::getCreateTime);
        return skillMapper.selectPage(pageParam, wrapper);
    }
}
