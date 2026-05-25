package com.prometheus.wallet.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prometheus.wallet.entity.Appeal;

public interface AppealService {

    /** 用户提交申诉，status 初始为 1-待处理 */
    void createAppeal(Appeal appeal);

    /** 管理员查看申诉列表，status 为 null 时查全部 */
    Page<Appeal> getAppeals(int page, int size, Integer status);

    /** 管理员处理申诉，更新 status=3、result、handledBy
     *  @param decision ACCEPT_REFUND=支持买家（退款取消订单）
     *                   ACCEPT_COMPLETE=支持卖家（强制完成并付款）
     *                   REJECT=驳回申诉（仅记录结果） */
    void handleAppeal(Long id, String result, Long adminId, String decision);
}
