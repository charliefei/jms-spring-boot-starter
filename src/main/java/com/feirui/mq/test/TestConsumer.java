package com.feirui.mq.test;

import com.feirui.mq.aspect.MQListener;
import com.feirui.mq.aspect.MQListenerContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author charliefei
 * @version V1.0
 * @description 描述信息
 * @date 2024/08/23 15:01 周五
 */
@MQListenerContainer
@Component
@Slf4j
public class TestConsumer {
    @MQListener(virtualTopic = "feirui-test")
    public void onMessage(String text) {
        log.info("TestConsumer接收到消息: {}", text);
    }
}
