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

    public boolean isTopic() {
        return StringUtils.hasText(topic);
    }
}
