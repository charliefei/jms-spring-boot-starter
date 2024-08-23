package com.feirui.mq;

import com.feirui.mq.aspect.MQListener;
import com.feirui.mq.aspect.MQListenerContainer;
import com.feirui.mq.domain.dto.MQRecvMessage;
import org.springframework.stereotype.Component;

/**
 * @author charliefei
 * @version V1.0
 * @description 描述信息
 * @date 2024/08/23 15:01 周五
 */
@MQListenerContainer
@Component
public class TestConsumer {

    @MQListener(topic = "test")
    public void onMessage(String text, MQRecvMessage recvMessage) {
        System.out.println(text);
        System.out.println(recvMessage);
    }
}
