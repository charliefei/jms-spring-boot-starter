package com.feirui.mq.service.impl;

import com.feirui.mq.config.MQConfigProperties;
import com.feirui.mq.domain.dto.MQRecvMessage;
import com.feirui.mq.domain.dto.MQSendMessage;
import com.feirui.mq.service.JmsService;
import com.feirui.mq.service.MQCallback;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

@Slf4j
public class TongMQService implements JmsService {
    private final MQConfigProperties.TongLQ tongLQ;
    private static Context context;
    private static ConnectionFactory queueConnFactory;
    private static TopicConnectionFactory topicConnFactory;

    @SneakyThrows
    public TongMQService(MQConfigProperties.TongLQ tongLQ) {
        this.tongLQ = tongLQ;
        context = createContext();
        queueConnFactory = (ConnectionFactory) context.lookup(tongLQ.getQueueFactory());
        topicConnFactory = (TopicConnectionFactory) context.lookup(tongLQ.getTopicFactory());
    }

    @Override
    public void sendMsgWithQueue(MQSendMessage message) throws Exception {
        Connection sendConn = null;
        Session sendSession = null;
        MessageProducer sendProducer = null;
        try {
            sendConn = queueConnFactory.createConnection();
            sendSession = sendConn.createSession(false, 1);
            sendConn.start();
            Queue sendQueue = (Queue) context.lookup(message.getQueue());
            sendProducer = sendSession.createProducer(sendQueue);
            TextMessage textMessage = sendSession.createTextMessage(message.getBody());
            sendProducer.send(textMessage);
            log.info("TongMQService {}消息发送QUEUE成功:{}", message.getQueue(), message.getBody());
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
        TopicConnection topicConn = null;
        TopicSession topicSession = null;
        TopicPublisher publisher = null;
        try {
            topicConn = topicConnFactory.createTopicConnection();
            topicSession = topicConn.createTopicSession(false, 1);
            topicConn.start();
            Topic topic = (Topic) context.lookup(message.getTopic());
            publisher = topicSession.createPublisher(topic);
            TextMessage textMessage = topicSession.createTextMessage(message.getBody());
            publisher.publish(textMessage);
            log.info("TongMQService {}消息发送TOPIC成功:{}", message.getTopic(), message.getBody());
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
        ConnectionFactory recvConnFactory;
        Connection recvConn = null;
        Session recvSession = null;
        Destination destination;
        try {
            if (recvMessage.isTopic()) {
                recvConnFactory = topicConnFactory;
                destination = (Topic) context.lookup(recvMessage.getTopic());
            } else {
                recvConnFactory = queueConnFactory;
                destination = (Queue) context.lookup(recvMessage.getQueue());
            }
            recvConn = recvConnFactory.createConnection();
            recvSession = recvConn.createSession(false, 1);
            recvConn.start();

            MessageConsumer recvConsumer = recvSession.createConsumer(destination);
            recvConsumer.setMessageListener(message -> {
                try {
                    TextMessage textMessage = (TextMessage) message;
                    callback.onMessage(textMessage.getText());
                } catch (Exception e) {
                    log.error("TongMQService==>{}==>监听失败", recvMessage);
                    log.error("TongMQService 异常日志:", e);
                }
            });
            log.info("TongMQService==>{}==>监听启动成功", recvMessage);
        } catch (Exception e) {
            log.error("TongMQService==>{}==>监听失败", recvMessage.toString());
            log.error("TongMQService 异常日志:", e);
            throw new Exception("TongMQService连接失败 brokerUrl:" + tongLQ.getNaming().getProviderUrl() + "异常信息:" + e.getMessage());
        } finally {
            if (recvConn != null) recvConn.close();
            if (recvSession != null) recvSession.close();
        }
    }

    private Context createContext() throws NamingException {
        Properties pro = new Properties();
        pro.setProperty(Context.INITIAL_CONTEXT_FACTORY, tongLQ.getNaming().getFactory());
        pro.setProperty(Context.PROVIDER_URL, tongLQ.getNaming().getProviderUrl());
        return new InitialContext(pro);
    }
}
