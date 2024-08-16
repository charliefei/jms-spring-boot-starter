package com.feirui.mq.service;

import com.feirui.mq.domain.dto.MQRecvMessage;

@FunctionalInterface
public interface MQCallback {
    void onMessage(String message, MQRecvMessage recvMessage) throws Exception;
}