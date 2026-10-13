package com.epicerie.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "epicerie.main.topic";
    public static final String INVENTORY_RESERVED_QUEUE = "q.order.inventory.reserved";
    public static final String INVENTORY_FAILED_QUEUE = "q.order.inventory.failed";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.order.created";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.order.cancelled";
    public static final String INVENTORY_RESERVED_ROUTING_KEY = "inventory.reserved";
    public static final String INVENTORY_FAILED_ROUTING_KEY = "inventory.failed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue inventoryReservedQueue() {
        return new Queue(INVENTORY_RESERVED_QUEUE);
    }

    @Bean
    public Queue inventoryFailedQueue() {
        return new Queue(INVENTORY_FAILED_QUEUE);
    }

    @Bean
    public Binding inventoryReservedBinding(Queue inventoryReservedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(inventoryReservedQueue).to(exchange).with(INVENTORY_RESERVED_ROUTING_KEY);
    }

    @Bean
    public Binding inventoryFailedBinding(Queue inventoryFailedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(inventoryFailedQueue).to(exchange).with(INVENTORY_FAILED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
