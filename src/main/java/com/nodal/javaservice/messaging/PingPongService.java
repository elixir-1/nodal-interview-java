package com.nodal.javaservice.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * PingPongService contains a RabbitListener and Publisher methods for "ping" and "pong" messages.
 * At startup, it will publish a "ping" message Q2, ready to be consumed by Kotlin service
 * Once the "ping" is consumed, a "pong" message will be received by the RabbitListener
 * When it receives a "ping" message, it will wait for a 10 seconds delay and initiate another "ping"
 */

@Service
public class PingPongService {

    // Define the Exchange name and Routing Key for RabbitMQ
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

    // Initiate the "ping" message on startup
    @PostConstruct
    public void initiateCommunication() {
        log.info("Initiating communication...");
        publishMessage(PING_MESSAGE);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = JAVA_SERVICE_EXCHANGE_NAME+"/"+Q1_ROUTING_KEY, durable = "false"),
            exchange = @Exchange(value = JAVA_SERVICE_EXCHANGE_NAME),
            key = Q1_ROUTING_KEY))
    public void consumeMessage(String message) {

        log.info("{} received message: {}", SERVICE_NAME, message);

        if (message.equals(PING_MESSAGE)) {
            publishMessage(PONG_MESSAGE);

            try {
                Thread.sleep(10000); // 10-second delay
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrupted: {}", e.getMessage());
            }

            publishMessage(PING_MESSAGE);
        }
    }

    public void publishMessage(String message) {

        try {
            rabbitTemplate.convertAndSend(KOTLIN_SERVICE_EXCHANGE_NAME, Q2_ROUTING_KEY, message);
            log.info("{} sent message: {}", SERVICE_NAME, message);
        } catch (AmqpException amqpException) {
            log.error("Failed to publish message to {}", Q2_ROUTING_KEY);
            log.error("Error: {}", String.valueOf(amqpException));
        }

    }
}
