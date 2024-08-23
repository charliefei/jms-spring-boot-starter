package com.feirui.mq;

import com.feirui.mq.domain.dto.MQSendMessage;
import com.feirui.mq.service.JmsService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

    @Resource
    JmsService jmsService;

    @Test
    void contextLoads() throws Exception {
        System.out.println(jmsService);
        MQSendMessage sendMessage = MQSendMessage.builder()
                .body("hello!!!")
                .topic("test")
                .build();
        jmsService.sendMsgWithTopic(sendMessage);
    }

}
