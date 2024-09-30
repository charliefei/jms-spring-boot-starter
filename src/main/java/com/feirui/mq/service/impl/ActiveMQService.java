package com.feirui.mq.service.impl;

import com.feirui.mq.config.MQConfigProperties;
import com.feirui.mq.domain.dto.MQRecvMessage;
import com.feirui.mq.domain.dto.MQSendMessage;
import com.feirui.mq.service.JmsService;
import com.feirui.mq.service.MQCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;

import javax.jms.*;

@Slf4j
public class ActiveMQService implements JmsService {
    private final MQConfigProperties.ActiveMQ activemq;
    private static ActiveMQConnectionFactory connectionFactory;
    @Value("${spring.application.name}")
    private String applicationName;

    public ActiveMQService(MQConfigProperties.ActiveMQ activemq) {
        this.activemq = activemq;
        connectionFactory = connectionFactory();
    }

    public ActiveMQConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory activeMqConnectionFactory = new ActiveMQConnectionFactory(activemq.getUser(),
                activemq.getPassword(),
                activemq.getBrokerUrl());
        activeMqConnectionFactory.setRedeliveryPolicy(redeliveryPolicy());
        return activeMqConnectionFactory;
    }

    public RedeliveryPolicy redeliveryPolicy() {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setMaximumRedeliveryDelay(-1L);
        redeliveryPolicy.setMaximumRedeliveries(3);
        redeliveryPolicy.setInitialRedeliveryDelay(1L);
        redeliveryPolicy.setBackOffMultiplier(5.0);
        redeliveryPolicy.setUseCollisionAvoidance(false);
        return redeliveryPolicy;
    }

    @Override
    public void sendMsgWithQueue(MQSendMessage message) throws Exception {
        sendMsg(message, false, false);
    }

    @Override
    public void sendMsgWithTopic(MQSendMessage message) throws Exception {
        sendMsg(message, true, false);
    }

    @Override
    public void sendMsgWithVirtualTopic(MQSendMessage message) throws Exception {
        sendMsg(message, false, true);
    }

    @Override
    public void recvMsg(MQRecvMessage recvMsg, MQCallback callback) throws Exception {
        Connection connection;
        Session session;
        Destination destination;
        MessageConsumer consumer;
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(Boolean.FALSE, 1);
            connection.start();
            if (recvMsg.isTopic()) {
                destination = session.createTopic(recvMsg.getTopic());
            } else if (recvMsg.isVirtualTopic()) {
                String queue = "Consumer." + applicationName + ".VirtualTopic." + recvMsg.getVirtualTopic();
                destination = session.createQueue(queue);
            } else {
                destination = session.createQueue(recvMsg.getQueue());
            }
            consumer = session.createConsumer(destination);
            consumer.setMessageListener(message -> {
                try {
                    TextMessage textMsg = (TextMessage) message;
                    callback.onMessage(textMsg.getText());
                } catch (Exception e) {
                    log.error("ActiveMQService==>{}==>监听失败", recvMsg);
                    log.error("ActiveMQService 异常日志:", e);
                }
            });
            log.info("ActiveMQ==>{}==>监听启动成功", recvMsg);
        } catch (Exception e) {
            log.error("ActiveMQService==>{}==>监听失败", recvMsg.toString());
            log.error("ActiveMQService 异常日志:", e);
            throw new Exception("ActiveMQService连接失败 brokerUrl:" + activemq.getBrokerUrl());
        }
    }

    private void sendMsg(MQSendMessage sendMessage, boolean isTopic, boolean isVirtualTopic) throws Exception {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        Destination destination;
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, 1);
            connection.start();
            if (isTopic) {
                destination = session.createTopic(sendMessage.getTopic());
            } else if (isVirtualTopic) {
                destination = session.createTopic("VirtualTopic." + sendMessage.getVirtualTopic());
            } else {
                destination = session.createQueue(sendMessage.getQueue());
            }
            producer = session.createProducer(destination);
            TextMessage msg = session.createTextMessage();
            msg.setText(sendMessage.getBody());
            producer.send(msg);
            log.info("发送ActiveMQService消息成功====>sendMessage={}", sendMessage);
        } catch (Exception e) {
            log.error("发送ActiveMQService消息异常====>sendMessage={} error={}", sendMessage, e.getMessage());
        } finally {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        }
    }

}
