package com.feirui.mq.aspect;

import com.feirui.mq.domain.dto.MQRecvMessage;
import com.feirui.mq.service.MQService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Map;

@Component
@Slf4j
public class MQListenerScanner implements ApplicationListener<ContextRefreshedEvent> {
    private static Integer errorCount = 0;
    private static final Integer[] errorTime = new Integer[]{10000, 60000, 1800000};

    @Override
    @SneakyThrows
    public void onApplicationEvent(ContextRefreshedEvent event) {
        MQService service = event.getApplicationContext().getBean(MQService.class);
        if (event.getApplicationContext().getParent() == null
                || event.getApplicationContext().getParent().getParent() == null
                || event.getApplicationContext().getParent().getParent().getParent() == null) {
            Map<String, Object> beans = event.getApplicationContext().getBeansWithAnnotation(MQListenerContainer.class);
            for (Object bean : beans.values()) {
                boolean isReload = false;
                Method[] declaredMethods = bean.getClass().getDeclaredMethods();
                for (Method method : declaredMethods) {
                    MQListener mqListener = AnnotationUtils.findAnnotation(method, MQListener.class);
                    if (mqListener != null) {
                        ReflectionUtils.makeAccessible(method);
                        Class<?>[] types = method.getParameterTypes();
                        if (types.length != 2 || types[0] != String.class || types[1] != MQRecvMessage.class) {
                            throw new IllegalArgumentException("错误的参数类型，请参考MQCallback.onMessage() ：" + method);
                        }

                        MQRecvMessage mqRecvMessage = new MQRecvMessage(mqListener.queue(), mqListener.topic());
                        try {
                            // 核心
                            service.recvMsg(mqRecvMessage, (message, recvMessage) -> method.invoke(bean, message, recvMessage));
                        } catch (Exception var16) {
                            log.error("MQ 启动消息监听，链接失败", var16);
                            isReload = true;
                            break;
                        }
                    }
                }

                if (isReload && errorCount < 3) {
                    reLoadListener(event);
                }
            }
        }
    }

    private synchronized void reLoadListener(ContextRefreshedEvent event) throws InterruptedException {
        log.error("MQ 监听第{}链接失败...{}秒后重连", errorCount + 1, errorTime[errorCount] / 1000);
        Thread.sleep((long) errorTime[errorCount]);
        errorCount = errorCount + 1;
        onApplicationEvent(event);
    }
}
