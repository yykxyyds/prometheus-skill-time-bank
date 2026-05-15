package com.prometheus.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prometheus.wallet.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
