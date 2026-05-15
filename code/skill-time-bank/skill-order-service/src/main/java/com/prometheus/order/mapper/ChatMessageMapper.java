package com.prometheus.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.prometheus.order.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}
