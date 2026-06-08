package com.prometheus.skill.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.order.entity.SkillOrder;
import com.prometheus.order.mapper.SkillOrderMapper;
import com.prometheus.skill.entity.Bounty;
import com.prometheus.skill.entity.BountyApplication;
import com.prometheus.skill.mapper.BountyApplicationMapper;
import com.prometheus.skill.mapper.BountyMapper;
import com.prometheus.skill.service.BountyService;
import com.prometheus.user.entity.User;
import com.prometheus.user.mapper.UserMapper;
import com.prometheus.user.service.NotificationService;
import com.prometheus.wallet.entity.Appeal;
import com.prometheus.wallet.entity.TimeTransaction;
import com.prometheus.wallet.mapper.AppealMapper;
import com.prometheus.wallet.mapper.TimeTransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 悬赏服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BountyServiceImpl implements BountyService {

    private final BountyMapper bountyMapper;
    private final BountyApplicationMapper applicationMapper;
    private final NotificationService notificationService;
    private final SkillOrderMapper skillOrderMapper;
    private final UserMapper userMapper;
    private final TimeTransactionMapper timeTransactionMapper;
    private final AppealMapper appealMapper;

    @Override
    public Page<Bounty> getBountyList(int page, int size, Integer status, String keyword, Long categoryId, String type, Long userId) {
        Page<Bounty> pageParam = new Page<>(page, size);
        return bountyMapper.selectPageWithUser(pageParam, status, keyword, categoryId, type, userId);
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
        bounty.setStatus(1); // 直接发布
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

        // 通知悬赏发布者：有人申请了你的悬赏（失败不影响业务）
        try {
            notificationService.sendNotification(bounty.getUserId(), "BOUNTY",
                    "新的悬赏申请", "有人申请了你的悬赏「" + bounty.getTitle() + "」", bountyId);
        } catch (Exception e) {
            log.warn("发送通知失败（已忽略）: {}", e.getMessage());
        }
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

        // 创建订单并冻结买方时间币
        createOrderForBounty(bounty, application.getApplicantId());

        // 拒绝该悬赏的其他申请
        LambdaUpdateWrapper<BountyApplication> rejectWrapper = new LambdaUpdateWrapper<>();
        rejectWrapper.eq(BountyApplication::getBountyId, bountyId)
                .ne(BountyApplication::getId, applicationId)
                .eq(BountyApplication::getStatus, 1)
                .set(BountyApplication::getStatus, 3)
                .set(BountyApplication::getUpdateTime, LocalDateTime.now());
        applicationMapper.update(null, rejectWrapper);

        // 通知被接受的申请人（失败不影响业务）
        try {
            notificationService.sendNotification(application.getApplicantId(), "BOUNTY",
                    "申请已通过", "你申请的悬赏「" + bounty.getTitle() + "」已被接受", bountyId);
        } catch (Exception e) {
            log.warn("发送通知失败（已忽略）: {}", e.getMessage());
        }

        // 通知其他被拒绝的申请人（失败不影响业务）
        List<BountyApplication> others = applicationMapper.selectList(
                new LambdaQueryWrapper<BountyApplication>()
                        .eq(BountyApplication::getBountyId, bountyId)
                        .ne(BountyApplication::getId, applicationId)
                        .eq(BountyApplication::getStatus, 3));
        for (BountyApplication other : others) {
            try {
                notificationService.sendNotification(other.getApplicantId(), "BOUNTY",
                        "申请未通过", "你申请的悬赏「" + bounty.getTitle() + "」已有他人接单", bountyId);
            } catch (Exception e) {
                log.warn("发送通知失败（已忽略）: {}", e.getMessage());
            }
        }

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

        // 通知被拒绝的申请人（失败不影响业务）
        try {
            notificationService.sendNotification(application.getApplicantId(), "BOUNTY",
                    "申请未通过", "你申请的悬赏「" + bounty.getTitle() + "」已被拒绝", bountyId);
        } catch (Exception e) {
            log.warn("发送通知失败（已忽略）: {}", e.getMessage());
        }

        log.info("用户 {} 拒绝了悬赏 {} 的申请 {}", ownerId, bountyId, applicationId);
    }

    @Override
    @Transactional
    public void completeBounty(Long bountyId, Long ownerId) {
        // 发布者（买方）确认完成
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        if (!bounty.getUserId().equals(ownerId)) {
            throw new BusinessException("只有发布者可以确认完成");
        }
        if (bounty.getStatus() != 2) {
            throw new BusinessException("悬赏状态不允许确认完成");
        }

        SkillOrder order = skillOrderMapper.selectByBountyIdForUpdate(bountyId);
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }
        if (order.getBuyerConfirm() != null && order.getBuyerConfirm() == 1) {
            throw new BusinessException("您已确认完成，请勿重复操作");
        }

        int sellerConfirm = order.getSellerConfirm() == null ? 0 : order.getSellerConfirm();
        if (sellerConfirm == 1) {
            // 双方都已确认（卖方已先确认）→ 直接完成
            order.setBuyerConfirm(1);
            order.setUpdateTime(LocalDateTime.now());
            skillOrderMapper.updateById(order);
            doCompleteBounty(bounty, order);
        } else {
            // 仅买方确认
            order.setBuyerConfirm(1);
            order.setUpdateTime(LocalDateTime.now());
            skillOrderMapper.updateById(order);

            try {
                notificationService.sendNotification(bounty.getApplicantId(), "BOUNTY",
                        "请确认完成", "发布者已确认完成悬赏「" + bounty.getTitle() + "」，请你确认", bountyId);
            } catch (Exception e) {
                log.warn("发送通知失败（已忽略）: {}", e.getMessage());
            }
        }

        log.info("买方确认完成悬赏: bountyId={}, userId={}", bountyId, ownerId);
    }

    @Override
    @Transactional
    public void applicantConfirmComplete(Long bountyId, Long applicantId) {
        // 接单人（卖方）确认完成
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        if (bounty.getApplicantId() == null || !bounty.getApplicantId().equals(applicantId)) {
            throw new BusinessException("只有接单人可确认完成");
        }
        if (bounty.getStatus() != 2) {
            throw new BusinessException("悬赏状态不允许确认完成");
        }

        SkillOrder order = skillOrderMapper.selectByBountyIdForUpdate(bountyId);
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }
        if (order.getSellerConfirm() != null && order.getSellerConfirm() == 1) {
            throw new BusinessException("您已确认完成，请勿重复操作");
        }

        int buyerConfirm = order.getBuyerConfirm() == null ? 0 : order.getBuyerConfirm();
        if (buyerConfirm == 1) {
            // 双方都已确认（发布者已先确认）→ 直接完成
            order.setSellerConfirm(1);
            order.setUpdateTime(LocalDateTime.now());
            skillOrderMapper.updateById(order);
            doCompleteBounty(bounty, order);
        } else {
            // 仅卖方确认
            order.setSellerConfirm(1);
            order.setUpdateTime(LocalDateTime.now());
            skillOrderMapper.updateById(order);

            try {
                notificationService.sendNotification(bounty.getUserId(), "BOUNTY",
                        "接单人已确认完成", "接单人已确认完成悬赏「" + bounty.getTitle() + "」，请你确认", bountyId);
            } catch (Exception e) {
                log.warn("发送通知失败（已忽略）: {}", e.getMessage());
            }
        }

        log.info("卖方确认完成悬赏: bountyId={}, userId={}", bountyId, applicantId);
    }

    /**
     * 双方确认后完成悬赏：状态设为已完成，并完成订单转账
     */
    private void doCompleteBounty(Bounty bounty, SkillOrder order) {
        // 更新悬赏状态
        bounty.setStatus(3); // 已完成
        bounty.setUpdateTime(LocalDateTime.now());
        bountyMapper.updateById(bounty);

        // 完成订单转账
        completeBountyOrder(bounty);

        // 通知双方（失败不影响业务）
        try {
            notificationService.sendNotification(bounty.getUserId(), "BOUNTY",
                    "悬赏已完成", "悬赏「" + bounty.getTitle() + "」已完成，时间币已转给接单人", bounty.getId());
        } catch (Exception e) {
            log.warn("发送通知失败（已忽略）: {}", e.getMessage());
        }
        if (bounty.getApplicantId() != null) {
            try {
                notificationService.sendNotification(bounty.getApplicantId(), "BOUNTY",
                        "悬赏已完成", "悬赏「" + bounty.getTitle() + "」已完成，时间币已到账", bounty.getId());
            } catch (Exception e) {
                log.warn("发送通知失败（已忽略）: {}", e.getMessage());
            }
        }

        log.info("悬赏双方确认完成, bountyId={}, orderId={}, amount={}",
                bounty.getId(), order.getId(), order.getAmount());
    }

    @Override
    @Transactional
    public void cancelBountyOrder(Long bountyId, Long userId) {
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }

        SkillOrder order = skillOrderMapper.selectByBountyIdForUpdate(bountyId);
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException("只有参与方可以取消");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("当前状态不可取消");
        }

        // 退款：解冻买方时间币
        User buyer = userMapper.selectByIdForUpdate(order.getBuyerId());
        if (buyer != null) {
            int frozenAmount = order.getFrozenAmount() == null ? order.getAmount() : order.getFrozenAmount();
            int newBalance = (buyer.getBalance() == null ? 0 : buyer.getBalance()) + frozenAmount;
            buyer.setBalance(newBalance);
            buyer.setFrozenBalance(Math.max((buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance()) - frozenAmount, 0));
            buyer.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(buyer);

            TimeTransaction tx = new TimeTransaction();
            tx.setUserId(buyer.getId());
            tx.setOrderId(order.getId());
            tx.setType("UNFREEZE");
            tx.setAmount(frozenAmount);
            tx.setBalanceAfter(newBalance);
            tx.setRemark("悬赏「" + bounty.getTitle() + "」取消解冻");
            tx.setCreateTime(LocalDateTime.now());
            timeTransactionMapper.insert(tx);
        }

        // 更新订单状态
        order.setStatus(5); // 已取消
        order.setFrozenAmount(0);
        order.setUpdateTime(LocalDateTime.now());
        skillOrderMapper.updateById(order);

        // 恢复悬赏状态到已发布（可重新接单）
        bounty.setStatus(1);
        bounty.setApplicantId(null);
        bounty.setUpdateTime(LocalDateTime.now());
        bountyMapper.updateById(bounty);

        // 清除旧的申请记录，允许重新申请
        LambdaUpdateWrapper<BountyApplication> delWrapper = new LambdaUpdateWrapper<>();
        delWrapper.eq(BountyApplication::getBountyId, bountyId);
        applicationMapper.delete(delWrapper);

        log.info("悬赏订单已取消并退款: bountyId={}, userId={}", bountyId, userId);
    }

    @Override
    public SkillOrder getBountyOrder(Long bountyId) {
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        SkillOrder order = skillOrderMapper.selectByBountyId(bountyId);
        if (order == null) {
            throw new BusinessException("关联订单不存在");
        }
        // 填充用户名
        User buyer = userMapper.selectById(order.getBuyerId());
        User seller = userMapper.selectById(order.getSellerId());
        if (buyer != null) {
            order.setBuyerName(buyer.getUsername());
            order.setBuyerAvatar(buyer.getAvatar());
        }
        if (seller != null) {
            order.setSellerName(seller.getUsername());
            order.setSellerAvatar(seller.getAvatar());
        }
        order.setBountyTitle(bounty.getTitle());
        return order;
    }

    @Override
    @Transactional
    public void createBountyAppeal(Long bountyId, Long userId, Appeal appeal) {
        SkillOrder order = skillOrderMapper.selectByBountyId(bountyId);
        if (order == null) {
            throw new BusinessException("关联订单不存在，无法申诉");
        }
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        appeal.setOrderId(order.getId());
        appeal.setUserId(userId);
        appeal.setStatus(1); // 待处理
        LocalDateTime now = LocalDateTime.now();
        appeal.setCreateTime(now);
        appeal.setUpdateTime(now);
        appealMapper.insert(appeal);
        log.info("悬赏申诉提交成功: id={}, bountyId={}, orderId={}, userId={}",
                appeal.getId(), bountyId, order.getId(), userId);
    }

    @Override
    public Bounty getBountyDetail(Long id) {
        Bounty bounty = bountyMapper.selectByIdWithUser(id);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        return bounty;
    }

    @Override
    @Transactional
    public void updateBounty(Bounty bounty, Long userId) {
        Bounty existing = bountyMapper.selectById(bounty.getId());
        if (existing == null) {
            throw new BusinessException("悬赏不存在");
        }
        if (!existing.getUserId().equals(userId)) {
            throw new BusinessException("只能编辑自己的悬赏");
        }
        if (existing.getStatus() != 0 && existing.getStatus() != 1) {
            throw new BusinessException("当前状态不允许编辑");
        }
        if (bounty.getReward() != null && bounty.getReward() <= 0) {
            throw new BusinessException("悬赏金额必须大于0");
        }
        bounty.setUpdateTime(LocalDateTime.now());
        bountyMapper.updateById(bounty);
        log.info("用户 {} 编辑了悬赏 {}", userId, bounty.getId());
    }

    @Override
    @Transactional
    public void deleteBounty(Long id, Long userId) {
        Bounty bounty = bountyMapper.selectById(id);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        if (!bounty.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的悬赏");
        }
        if (bounty.getStatus() != 0 && bounty.getStatus() != 1) {
            throw new BusinessException("当前状态不允许删除");
        }
        // 同时删除该悬赏的申请记录
        LambdaUpdateWrapper<BountyApplication> delWrapper = new LambdaUpdateWrapper<>();
        delWrapper.eq(BountyApplication::getBountyId, id);
        applicationMapper.delete(delWrapper);
        bountyMapper.deleteById(id);
        log.info("用户 {} 删除了悬赏 {}", userId, id);
    }

    @Override
    public List<BountyApplication> getApplications(Long bountyId, Long userId) {
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BusinessException("悬赏不存在");
        }
        if (!bounty.getUserId().equals(userId)) {
            throw new BusinessException("只能查看自己悬赏的申请");
        }
        LambdaQueryWrapper<BountyApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BountyApplication::getBountyId, bountyId)
                .orderByDesc(BountyApplication::getCreateTime);
        return applicationMapper.selectList(wrapper);
    }

    /**
     * 悬赏接单后创建订单并冻结买方时间币
     */
    private void createOrderForBounty(Bounty bounty, Long sellerId) {
        User buyer = userMapper.selectByIdForUpdate(bounty.getUserId());
        if (buyer == null) {
            throw new BusinessException("悬赏发布者不存在");
        }
        int balance = buyer.getBalance() == null ? 0 : buyer.getBalance();
        if (balance < bounty.getReward()) {
            throw new BusinessException("时间币余额不足，无法创建订单");
        }

        // 创建订单（状态直接为"进行中"）
        SkillOrder order = new SkillOrder();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setBuyerId(bounty.getUserId());
        order.setSellerId(sellerId);
        order.setBountyId(bounty.getId());
        order.setAmount(bounty.getReward());
        order.setFrozenAmount(bounty.getReward());
        order.setStatus(2); // 进行中
        order.setBuyerConfirm(0);
        order.setSellerConfirm(0);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        skillOrderMapper.insert(order);

        // 冻结买方时间币
        int frozen = buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance();
        buyer.setBalance(balance - bounty.getReward());
        buyer.setFrozenBalance(frozen + bounty.getReward());
        buyer.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(buyer);

        // 记录冻结流水
        TimeTransaction freezeTx = new TimeTransaction();
        freezeTx.setUserId(bounty.getUserId());
        freezeTx.setOrderId(order.getId());
        freezeTx.setType("FREEZE");
        freezeTx.setAmount(bounty.getReward());
        freezeTx.setBalanceAfter(balance - bounty.getReward());
        freezeTx.setRemark("悬赏「" + bounty.getTitle() + "」冻结时间币");
        freezeTx.setCreateTime(LocalDateTime.now());
        timeTransactionMapper.insert(freezeTx);

        log.info("悬赏订单已创建并冻结时间币: orderId={}, bountyId={}, buyerId={}, sellerId={}, amount={}",
                order.getId(), bounty.getId(), bounty.getUserId(), sellerId, bounty.getReward());
    }

    /**
     * 悬赏完成后转账并完成订单
     */
    private void completeBountyOrder(Bounty bounty) {
        // 查找关联订单（FOR UPDATE 悲观锁）
        SkillOrder order = skillOrderMapper.selectByBountyIdForUpdate(bounty.getId());
        if (order == null) {
            log.warn("悬赏 {} 未找到关联订单，跳过转账", bounty.getId());
            return;
        }
        if (order.getStatus() == 4) {
            log.warn("悬赏 {} 关联订单已完成，跳过重复转账", bounty.getId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 解冻买方时间币
        User buyer = userMapper.selectByIdForUpdate(order.getBuyerId());
        if (buyer != null) {
            int frozen = (buyer.getFrozenBalance() == null ? 0 : buyer.getFrozenBalance()) - order.getAmount();
            buyer.setFrozenBalance(Math.max(frozen, 0));
            buyer.setUpdateTime(now);
            userMapper.updateById(buyer);

            // 支出流水
            TimeTransaction expenseTx = new TimeTransaction();
            expenseTx.setUserId(buyer.getId());
            expenseTx.setOrderId(order.getId());
            expenseTx.setType("EXPENSE");
            expenseTx.setAmount(order.getAmount());
            expenseTx.setBalanceAfter(buyer.getBalance() == null ? 0 : buyer.getBalance());
            expenseTx.setRemark("悬赏「" + bounty.getTitle() + "」完成支出");
            expenseTx.setCreateTime(now);
            timeTransactionMapper.insert(expenseTx);
        }

        // 卖方到账
        User seller = userMapper.selectByIdForUpdate(order.getSellerId());
        if (seller != null) {
            int sellerBalance = (seller.getBalance() == null ? 0 : seller.getBalance()) + order.getAmount();
            seller.setBalance(sellerBalance);
            seller.setUpdateTime(now);
            userMapper.updateById(seller);

            // 收入流水
            TimeTransaction incomeTx = new TimeTransaction();
            incomeTx.setUserId(seller.getId());
            incomeTx.setOrderId(order.getId());
            incomeTx.setType("INCOME");
            incomeTx.setAmount(order.getAmount());
            incomeTx.setBalanceAfter(sellerBalance);
            incomeTx.setRemark("悬赏「" + bounty.getTitle() + "」完成收入");
            incomeTx.setCreateTime(now);
            timeTransactionMapper.insert(incomeTx);
        }

        // 更新订单状态
        order.setStatus(4); // 已完成
        order.setFrozenAmount(0);
        order.setCompletedTime(now);
        order.setUpdateTime(now);
        skillOrderMapper.updateById(order);

        log.info("悬赏订单已完成转账: orderId={}, bountyId={}, amount={}", order.getId(), bounty.getId(), order.getAmount());
    }
}
