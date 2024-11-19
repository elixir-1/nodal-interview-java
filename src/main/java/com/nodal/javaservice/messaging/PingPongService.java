package com.nodal.javaservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * PingPongService will create a queue listener
 */

@Service
public class PingPongService {

    public static final String JAVA_SERVICE_EXCHANGE_NAME = "com.javaservice.exchange";
    public static final String KOTLIN_SERVICE_EXCHANGE_NAME = "com.kotlinservice.exchange";
    public static final String Q1_ROUTING_KEY = "queue1.routing.key";
    public static final String Q2_ROUTING_KEY = "queue2.routing.key";

    public static final String PING_MESSAGE = "ping";
    public static final String PONG_MESSAGE = "pong";

    public static final String SERVICE_NAME = "java service";

    public static final Logger log = LoggerFactory.getLogger(PingPongService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void initiateCommunication() {
        publishMessage(PING_MESSAGE);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = JAVA_SERVICE_EXCHANGE_NAME+"/"+Q1_ROUTING_KEY, durable = "true"),
            exchange = @Exchange(value = JAVA_SERVICE_EXCHANGE_NAME, ignoreDeclarationExceptions = "true"),
            key = Q1_ROUTING_KEY))
    public void consumeMessage(String message) {

        log.info("{} received message: {}", SERVICE_NAME, message);

        if (message.equals(PING_MESSAGE)) {
            publishMessage(PONG_MESSAGE);
        }

        else {
            try {
                Thread.sleep(10000); // 10-second delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread interrupted: " + e.getMessage());
            }

            publishMessage(PING_MESSAGE);
        }
    }

    public void publishMessage(String message) {

        rabbitTemplate.convertAndSend(KOTLIN_SERVICE_EXCHANGE_NAME, Q2_ROUTING_KEY, message);
        log.info("{} sent message: {}", SERVICE_NAME, message);
    }
}
