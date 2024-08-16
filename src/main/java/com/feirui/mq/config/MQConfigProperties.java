package com.feirui.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mq")
public class MQConfigProperties {
    private String type;
    private ActiveMQ activemq;
    private RocketMQ rocketmq;

    @Data
    public static class ActiveMQ {
        private String user;
        private String password;
        private String brokerUrl;
    }

    @Data
    public static class RocketMQ {

    }
}
