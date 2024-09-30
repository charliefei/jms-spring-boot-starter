package com.feirui.mq.service;

import com.feirui.mq.domain.dto.MQRecvMessage;
import com.feirui.mq.domain.dto.MQSendMessage;

public interface JmsService {
    void sendMsgWithQueue(MQSendMessage message) throws Exception;

    void sendMsgWithTopic(MQSendMessage message) throws Exception;

    void sendMsgWithVirtualTopic(MQSendMessage message) throws Exception;

    void recvMsg(MQRecvMessage message, MQCallback callback) throws Exception;
}
