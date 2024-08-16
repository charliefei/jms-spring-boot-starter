package com.feirui.mq.domain.dto;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MQSendMessage implements Serializable {
    private MQConfig config;
    private String body;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MQConfig {
        private String queue;
        private String topic;
    }
}