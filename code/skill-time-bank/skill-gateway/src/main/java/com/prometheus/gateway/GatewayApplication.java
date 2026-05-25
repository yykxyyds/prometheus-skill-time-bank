package com.prometheus.gateway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan("com.prometheus")
@MapperScan({"com.prometheus.user.mapper", "com.prometheus.skill.mapper", "com.prometheus.order.mapper", "com.prometheus.wallet.mapper"})
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
