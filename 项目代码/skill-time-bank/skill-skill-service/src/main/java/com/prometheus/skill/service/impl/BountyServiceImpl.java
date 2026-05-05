package com.prometheus.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.skill.entity.Bounty;
import com.prometheus.skill.entity.BountyApplication;
import com.prometheus.skill.mapper.BountyApplicationMapper;
import com.prometheus.skill.mapper.BountyMapper;
import com.prometheus.skill.service.BountyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 悬赏服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BountyServiceImpl implements BountyService {

    private final BountyMapper bountyMapper;
    private final BountyApplicationMapper applicationMapper;

    @Override
    public Page<Bounty> getBountyList(int page, int size, Integer status) {
        Page<Bounty> pageParam = new Page<>(page, size);
        return bountyMapper.selectPageWithUser(pageParam, status);
    }

    @Override
    @Transactional
    public void publishBounty(Bounty bounty) {
        if (bounty.getReward() == null || bounty.getReward() <= 0) {
            throw new BusinessException("悬赏金额必须大于0");
        }
        if (bounty.getTitle() == null || bounty.getTitle().trim().isEmpty()) {
            throw new BusinessException("悬赏标题不能为空");
        }
        bounty.setStatus(1); // 已发布
        bounty.setCreateTime(LocalDateTime.now());
        bounty.setUpdateTime(LocalDateTime.now());
        bountyMapper.insert(bounty);
        log.info("用户 {} 发布了悬赏: {}", bounty.getUserId(), bounty.getTitle());
    }

    @Override
    @Transactional
    public void applyBounty(Long bountyId, Long applicantId, String message) {
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        if (bounty.getStatus() != 1) {
            throw new BusinessException("该悬赏已不可申请");
        }
        if (bounty.getUserId().equals(applicantId)) {
            throw new BusinessException("不能申请自己的悬赏");
        }

        // 检查是否已申请过
        LambdaQueryWrapper<BountyApplication> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(BountyApplication::getBountyId, bountyId)
                .eq(BountyApplication::getApplicantId, applicantId);
        Long count = applicationMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusinessException("您已申请过该悬赏");
        }

        BountyApplication application = new BountyApplication();
        application.setBountyId(bountyId);
        application.setApplicantId(applicantId);
        application.setMessage(message);
        application.setStatus(1); // 待确认
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.insert(application);
        log.info("用户 {} 申请了悬赏 {}", applicantId, bountyId);
    }

    @Override
    @Transactional
    public void acceptApplication(Long bountyId, Long applicationId, Long ownerId) {
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        if (!bounty.getUserId().equals(ownerId)) {
            throw new BusinessException("只能操作自己的悬赏");
        }
        if (bounty.getStatus() != 1) {
            throw new BusinessException("该悬赏已有人接单或已完成");
        }

        BountyApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        if (!application.getBountyId().equals(bountyId)) {
            throw new BusinessException("申请与悬赏不匹配");
        }
        if (application.getStatus() != 1) {
            throw new BusinessException("该申请已被处理");
        }

        // 接受该申请，拒绝其他申请
        application.setStatus(2); // 已接受
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);

        // 更新悬赏状态为已接单
        bounty.setStatus(2);
        bounty.setApplicantId(application.getApplicantId());
        bounty.setUpdateTime(LocalDateTime.now());
        bountyMapper.updateById(bounty);

        // 拒绝该悬赏的其他申请
        LambdaUpdateWrapper<BountyApplication> rejectWrapper = new LambdaUpdateWrapper<>();
        rejectWrapper.eq(BountyApplication::getBountyId, bountyId)
                .ne(BountyApplication::getId, applicationId)
                .eq(BountyApplication::getStatus, 1)
                .set(BountyApplication::getStatus, 3)
                .set(BountyApplication::getUpdateTime, LocalDateTime.now());
        applicationMapper.update(null, rejectWrapper);

        log.info("用户 {} 接受了悬赏 {} 的申请 {}", ownerId, bountyId, applicationId);
    }

    @Override
    @Transactional
    public void rejectApplication(Long bountyId, Long applicationId, Long ownerId) {
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        if (!bounty.getUserId().equals(ownerId)) {
            throw new BusinessException("只能操作自己的悬赏");
        }

        BountyApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            throw new BusinessException("申请不存在");
        }
        if (!application.getBountyId().equals(bountyId)) {
            throw new BusinessException("申请与悬赏不匹配");
        }
        if (application.getStatus() != 1) {
            throw new BusinessException("该申请已被处理");
        }

        application.setStatus(3); // 已拒绝
        application.setUpdateTime(LocalDateTime.now());
        applicationMapper.updateById(application);
        log.info("用户 {} 拒绝了悬赏 {} 的申请 {}", ownerId, bountyId, applicationId);
    }

    @Override
    @Transactional
    public void completeBounty(Long bountyId, Long ownerId) {
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        if (!bounty.getUserId().equals(ownerId)) {
            throw new BusinessException("只能操作自己的悬赏");
        }
        if (bounty.getStatus() != 2) {
            throw new BusinessException("悬赏状态不允许确认完成");
        }

        bounty.setStatus(3); // 已完成
        bounty.setUpdateTime(LocalDateTime.now());
        bountyMapper.updateById(bounty);

        log.info("悬赏 {} 已完成，时间币待 order-service 转账", bountyId);
    }

    @Override
    public Bounty getBountyDetail(Long id) {
        Bounty bounty = bountyMapper.selectByIdWithUser(id);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        return bounty;
    }
}
