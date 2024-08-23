package com.feirui.mq.domain.dto;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MQSendMessage implements Serializable {
    private String body;
    private String queue;
    private String topic;
}