package com.feirui.mq;

import com.feirui.mq.service.MQService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

    @Resource
    MQService mqService;

    @Test
    void contextLoads() {
        System.out.println(mqService);
    }

}
