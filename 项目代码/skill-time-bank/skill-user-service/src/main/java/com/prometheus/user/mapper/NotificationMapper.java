package com.prometheus.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prometheus.user.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
