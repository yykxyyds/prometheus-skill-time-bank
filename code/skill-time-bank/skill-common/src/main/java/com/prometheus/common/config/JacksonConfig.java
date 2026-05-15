package com.prometheus.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 应用启动后，向所有 Jackson 消息转换器注入 Long→String 序列化器，
 * 防止 Snowflake ID 在 JS 中精度丢失。
 */
@Slf4j
@Configuration
public class JacksonConfig implements ApplicationListener<ContextRefreshedEvent> {

    private static final DateTimeFormatter SPACE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        RequestMappingHandlerAdapter adapter = ctx.getBean(RequestMappingHandlerAdapter.class);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(SPACE_FMT));
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(SPACE_FMT));

        SimpleModule longModule = new SimpleModule("LongToStringModule");
        longModule.addSerializer(Long.class, ToStringSerializer.instance);
        longModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

        adapter.getMessageConverters().stream()
                .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                .map(c -> (MappingJackson2HttpMessageConverter) c)
                .forEach(converter -> {
                    ObjectMapper om = converter.getObjectMapper();
                    om.registerModule(javaTimeModule);
                    om.registerModule(longModule);
                });

        log.info("Long→String serializer injected into {} Jackson converter(s)",
                adapter.getMessageConverters().stream().filter(c -> c instanceof MappingJackson2HttpMessageConverter).count());
    }
}
