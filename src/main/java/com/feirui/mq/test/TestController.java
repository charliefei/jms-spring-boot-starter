package com.feirui.mq.test;

import com.feirui.mq.domain.dto.MQSendMessage;
import com.feirui.mq.service.JmsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Copyright (C), 群杰物联
 *
 * @author charliefei
 * @version V1.0
 * @description 描述信息
 * @date 2024/09/30 16:33 周一
 */
@RestController
public class TestController {

    @Resource
    JmsService jmsService;

    @GetMapping("/send")
    public String sendMsg() throws Exception {
        System.out.println(jmsService);
        MQSendMessage sendMessage = MQSendMessage.builder()
                .body("hello!!!")
                .virtualTopic("feirui-test")
                .build();
        jmsService.sendMsgWithVirtualTopic(sendMessage);
        return "ok";
    }

}
