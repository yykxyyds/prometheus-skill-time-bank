package com.prometheus.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.wallet.entity.Appeal;
import com.prometheus.wallet.mapper.AppealMapper;
import com.prometheus.wallet.service.AppealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final AppealMapper appealMapper;

    @Override
    @Transactional
    public void createAppeal(Appeal appeal) {
        appeal.setStatus(1); // 待处理
        LocalDateTime now = LocalDateTime.now();
        appeal.setCreateTime(now);
        appeal.setUpdateTime(now);
        appealMapper.insert(appeal);
        log.info("申诉提交成功: id={}, orderId={}, userId={}", appeal.getId(), appeal.getOrderId(), appeal.getUserId());
    }

    @Override
    public Page<Appeal> getAppeals(int pageNum, int size, Integer status) {
        Page<Appeal> page = new Page<>(pageNum, size);
        LambdaQueryWrapper<Appeal> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Appeal::getStatus, status);
        }
        wrapper.orderByDesc(Appeal::getCreateTime);
        return appealMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void handleAppeal(Long id, String result, Long adminId) {
        Appeal appeal = appealMapper.selectById(id);
        if (appeal == null) {
            throw new BusinessException(404, "申诉不存在");
        }
        if (appeal.getStatus() == 3) {
            throw new BusinessException(400, "该申诉已处理");
        }
        appeal.setStatus(3); // 已处理
        appeal.setResult(result);
        appeal.setHandledBy(adminId);
        appeal.setUpdateTime(LocalDateTime.now());
        appealMapper.updateById(appeal);
        log.info("申诉处理完成: id={}, adminId={}", id, adminId);
    }
}
