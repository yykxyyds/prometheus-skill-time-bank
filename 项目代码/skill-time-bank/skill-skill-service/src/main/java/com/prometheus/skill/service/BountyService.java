package com.prometheus.skill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.skill.entity.Bounty;

/**
 * 悬赏服务接口
 */
public interface BountyService {

    /**
     * 悬赏列表（分页+按状态+按类型筛选）
     *
     * @param type   筛选类型：null-全部, "publish"-我发布的, "take"-我接的, "complete"-我完成的
     * @param userId 当前用户 ID（type 不为 null 时必填）
     */
    Page<Bounty> getBountyList(int page, int size, Integer status, String type, Long userId);

    /**
     * 发布悬赏
     */
    void publishBounty(Bounty bounty);

    /**
     * 申请接悬赏
     */
    void applyBounty(Long bountyId, Long applicantId, String message);

    /**
     * 悬赏发布者接受申请
     */
    void acceptApplication(Long bountyId, Long applicationId, Long ownerId);

    /**
     * 悬赏发布者拒绝申请
     */
    void rejectApplication(Long bountyId, Long applicationId, Long ownerId);

    /**
     * 确认完成悬赏
     */
    void completeBounty(Long bountyId, Long ownerId);

    /**
     * 悬赏详情（含发布者用户名）
     */
    Bounty getBountyDetail(Long id);
}
