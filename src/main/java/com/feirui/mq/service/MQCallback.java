package com.feirui.mq.service;

import com.feirui.mq.domain.dto.MQRecvMessage;

@FunctionalInterface
public interface MQCallback {
    void onMessage(String textMsg, MQRecvMessage recvMsg) throws Exception;
}