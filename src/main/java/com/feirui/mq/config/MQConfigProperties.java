package com.feirui.mq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mq")
public class MQConfigProperties {
    private String type;
    private ActiveMQ activemq;
    private TongLQ tlq;

    @Data
    public static class ActiveMQ {
        private String user;
        private String password;
        private String brokerUrl;
    }

    @Data
    public static class TongLQ {
        private Naming naming;
        private String queueFactory;
        private String topicFactory;

        @Data
        public static class Naming {
            private String factory;
            private String providerUrl;
        }
    }
}
