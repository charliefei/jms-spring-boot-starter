package com.feirui.mq;

import com.feirui.mq.config.MQConfigProperties;
import com.feirui.mq.service.impl.ActiveMQService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MQConfigProperties.class)
public class MQServiceAutoConfiguration {
    @Bean
    @ConditionalOnProperty(name = "mq.type", havingValue = "activemq")
    public ActiveMQService activeMQService() {
        return new ActiveMQService();
    }
}
