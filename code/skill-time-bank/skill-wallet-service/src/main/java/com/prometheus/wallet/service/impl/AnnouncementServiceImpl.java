package com.prometheus.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.common.BusinessException;
import com.prometheus.wallet.entity.Announcement;
import com.prometheus.wallet.mapper.AnnouncementMapper;
import com.prometheus.wallet.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public Page<Announcement> getAnnouncements(int pageNum, int size) {
        Page<Announcement> page = new Page<>(pageNum, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Announcement::getIsTop)
                .orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public Announcement getAnnouncementDetail(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }
        // 增加浏览次数
        announcementMapper.update(null, new LambdaUpdateWrapper<Announcement>()
                .eq(Announcement::getId, id)
                .setSql("view_count = view_count + 1"));
        // 重新查询返回最新数据
        return announcementMapper.selectById(id);
    }

    @Override
    public void createAnnouncement(Announcement announcement, Long publisherId) {
        announcement.setPublisherId(publisherId);
        announcement.setViewCount(0);
        if (announcement.getIsTop() == null) {
            announcement.setIsTop(0);
        }
        LocalDateTime now = LocalDateTime.now();
        announcement.setCreateTime(now);
        announcement.setUpdateTime(now);
        announcementMapper.insert(announcement);
        log.info("公告发布成功: id={}, title={}", announcement.getId(), announcement.getTitle());
    }

    @Override
    public void updateAnnouncement(Announcement announcement) {
        Announcement existing = announcementMapper.selectById(announcement.getId());
        if (existing == null) {
            throw new BusinessException(404, "公告不存在");
        }
        announcement.setUpdateTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);
        log.info("公告更新成功: id={}", announcement.getId());
    }

    @Override
    public void deleteAnnouncement(Long id) {
        int rows = announcementMapper.deleteById(id);
        if (rows == 0) {
            throw new BusinessException(404, "公告不存在");
        }
        log.info("公告删除成功: id={}", id);
    }
}
