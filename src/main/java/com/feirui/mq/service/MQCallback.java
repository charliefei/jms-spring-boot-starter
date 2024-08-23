package com.feirui.mq.service;

@FunctionalInterface
public interface MQCallback {
    void onMessage(String textMsg) throws Exception;
}