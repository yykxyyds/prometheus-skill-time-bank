package com.prometheus.wallet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.wallet.entity.Announcement;

public interface AnnouncementService {

    /** 按置顶+时间排序分页查询公告 */
    Page<Announcement> getAnnouncements(int page, int size);

    /** 查看公告详情（同时增加浏览次数） */
    Announcement getAnnouncementDetail(Long id);

    /** 发布公告 */
    void createAnnouncement(Announcement announcement, Long publisherId);

    /** 更新公告 */
    void updateAnnouncement(Announcement announcement);

    /** 删除公告 */
    void deleteAnnouncement(Long id);
}
