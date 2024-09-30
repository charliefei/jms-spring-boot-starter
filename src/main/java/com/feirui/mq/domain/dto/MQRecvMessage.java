package com.feirui.mq.domain.dto;

import lombok.*;
import org.springframework.util.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class MQRecvMessage {
    private String queue;
    private String topic;
    private String virtualTopic;

    public boolean isTopic() {
        return StringUtils.hasText(topic);
    }

    public boolean isVirtualTopic() {
        return StringUtils.hasText(virtualTopic);
    }
}
