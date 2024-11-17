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

@Service
public class PingPongService {

    public static final String EXCHANGE_NAME = "com.javaservice";
    public static final String PONG_ROUTING_KEY = "Pongdom";
    public static final String PING_ROUTING_KEY = "Pingdom";
    public static final String PONG_QUEUE_NAME = EXCHANGE_NAME+"/"+PONG_ROUTING_KEY;
    public static final String PING_QUEUE_NAME = EXCHANGE_NAME+"/"+PING_ROUTING_KEY;

    public static final Logger log = LoggerFactory.getLogger(PingPongService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = PONG_QUEUE_NAME, durable = "true"),
            exchange = @Exchange(value = EXCHANGE_NAME, ignoreDeclarationExceptions = "true"),
            key = PONG_ROUTING_KEY))
    public void consumePong(String message) {

        log.info(message);
    }
}
