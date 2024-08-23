package com.feirui.mq.service.impl;

import com.feirui.mq.config.MQConfigProperties;
import com.feirui.mq.domain.dto.MQRecvMessage;
import com.feirui.mq.domain.dto.MQSendMessage;
import com.feirui.mq.service.MQCallback;
import com.feirui.mq.service.MQService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

@Slf4j
public class TongMQService implements MQService {
    private MQConfigProperties.TongLQ tongLQ;
    @Resource
    private MQConfigProperties mqConfigProperties;

    @PostConstruct
    public void init() {
        tongLQ = mqConfigProperties.getTlq();
    }

    @Override
    public void sendMsgWithQueue(MQSendMessage message) throws Exception {
        ConnectionFactory sendTongConnFactory;
        Connection sendConn = null;
        Session sendSession = null;
        Queue sendQueue;
        MessageProducer sendProducer = null;

        TextMessage textMessage;
        Context ctx = this.getContext();
        try {
            sendTongConnFactory = (ConnectionFactory) ctx.lookup(tongLQ.getQueueFactory());
            sendConn = sendTongConnFactory.createConnection();
            sendSession = sendConn.createSession(false, 1);
            sendConn.start();
            sendQueue = (Queue) ctx.lookup(message.getConfig().getQueue());
            sendProducer = sendSession.createProducer(sendQueue);
            textMessage = sendSession.createTextMessage(message.getBody());
            sendProducer.send(textMessage);
            log.info("TongMQService {}消息发送QUEUE成功:{}", message.getConfig().getQueue(), message.getBody());
        } catch (Exception e) {
            log.error("TongMQService=====>发送消息异常:", e);
        } finally {
            if (sendConn != null) sendConn.close();
            if (sendSession != null) sendSession.close();
            if (sendProducer != null) sendProducer.close();
        }
    }

    @Override
    public void sendMsgWithTopic(MQSendMessage message) throws Exception {
        TopicConnectionFactory topicConnFactory;
        TopicConnection topicConn = null;
        TopicSession topicSession = null;
        TopicPublisher publisher = null;
        Topic sendTopic;

        TextMessage textMessage;
        Context ctx = this.getContext();
        try {
            topicConnFactory = (TopicConnectionFactory) ctx.lookup(tongLQ.getTopicFactory());
            topicConn = topicConnFactory.createTopicConnection();
            topicSession = topicConn.createTopicSession(false, 1);
            sendTopic = (Topic) ctx.lookup(message.getConfig().getTopic());
            topicConn.start();
            publisher = topicSession.createPublisher(sendTopic);
            textMessage = topicSession.createTextMessage(message.getBody());
            publisher.publish(textMessage);
            log.info("TongMQService {}消息发送TOPIC成功:{}", message.getConfig().getTopic(), message.getBody());
        } catch (Exception e) {
            log.error("TongMQService=====>发送消息异常:", e);
        } finally {
            if (topicConn != null) topicConn.close();
            if (topicSession != null) topicSession.close();
            if (publisher != null) publisher.close();
        }
    }

    @Override
    public void recvMsg(MQRecvMessage recvMessage, MQCallback callback) throws Exception {
        Context context = this.getContext();
        try {
            if (recvMessage.isTopic()) {
                TopicConnectionFactory recvTopicConnFactory = (TopicConnectionFactory) context.lookup(tongLQ.getTopicFactory());
                TopicConnection recvTopicConn = recvTopicConnFactory.createTopicConnection();
                TopicSession recvTopicSession = recvTopicConn.createTopicSession(false, 1);
                Topic recvTopic = (Topic) context.lookup(recvMessage.getQueue());
                recvTopicConn.start();
                TopicSubscriber subscriber = recvTopicSession.createSubscriber(recvTopic);
                subscriber.setMessageListener(message -> {
                    try {
                        TextMessage textMessage = (TextMessage) message;
                        callback.onMessage(textMessage.getText(), recvMessage);
                    } catch (Exception e) {
                        log.error("ActiveMQService==>{}==>监听失败", recvMessage);
                        log.error("ActiveMQService 异常日志:", e);
                    }
                });
            } else {
                ConnectionFactory recvQueueConnFactory = (ConnectionFactory) context.lookup(tongLQ.getQueueFactory());
                Connection recvQueueConn = recvQueueConnFactory.createConnection();
                Session recvQueueSession = recvQueueConn.createSession(false, 1);
                Queue recvQueue = (Queue) context.lookup(recvMessage.getQueue());
                recvQueueConn.start();
                MessageConsumer recvConsumer = recvQueueSession.createConsumer(recvQueue);
                recvConsumer.setMessageListener(message -> {
                    try {
                        TextMessage textMessage = (TextMessage) message;
                        callback.onMessage(textMessage.getText(), recvMessage);
                    } catch (Exception e) {
                        log.error("ActiveMQService==>{}==>监听失败", recvMessage);
                        log.error("ActiveMQService 异常日志:", e);
                    }
                });
            }
            log.info("TongMQService==>{}==>监听启动成功", recvMessage);
        } catch (Exception e) {
            log.error("TongMQService==>{}==>监听失败", recvMessage.toString());
            log.error("TongMQService 异常日志:", e);
            throw new Exception("TongMQService连接失败 brokerUrl:" + tongLQ.getNaming().getUrl() + "异常信息:" + e.getMessage());
        }
    }

    private Properties getProperties() {
        Properties pro = new Properties();
        pro.setProperty("java.naming.factory.initial", tongLQ.getNaming().getFactory());
        pro.setProperty("java.naming.provider.url", tongLQ.getNaming().getUrl());
        return pro;
    }

    private Context getContext() {
        Properties pro = this.getProperties();
        Context ctx = null;
        try {
            ctx = new InitialContext(pro);
        } catch (NamingException e) {
            log.error("remoteURL:{}", tongLQ.getNaming().getUrl());
            log.error("NamingException:", e);
        }
        return ctx;
    }
}
