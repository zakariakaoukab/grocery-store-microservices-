package com.epicerie.product.config;

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

    // Queues
    public static final String ORDER_CREATED_QUEUE = "q.product.order.created";
    public static final String ORDER_CANCELLED_QUEUE = "q.product.order.cancelled";
    public static final String STOCK_SUPPLIED_QUEUE = "q.product.stock.supplied";

    // Routing keys
    public static final String ORDER_CREATED_ROUTING_KEY = "order.order.created";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.order.cancelled";
    public static final String STOCK_SUPPLIED_ROUTING_KEY = "supplier.stock.supplied";
    public static final String INVENTORY_RESERVED_ROUTING_KEY = "inventory.reserved";
    public static final String INVENTORY_FAILED_ROUTING_KEY = "inventory.failed";
    public static final String STOCK_LOW_ROUTING_KEY = "product.stock.low";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(exchange).with(ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Queue orderCancelledQueue() {
        return new Queue(ORDER_CANCELLED_QUEUE);
    }

    @Bean
    public Binding orderCancelledBinding(Queue orderCancelledQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderCancelledQueue).to(exchange).with(ORDER_CANCELLED_ROUTING_KEY);
    }

    @Bean
    public Queue stockSuppliedQueue() {
        return new Queue(STOCK_SUPPLIED_QUEUE);
    }

    @Bean
    public Binding stockSuppliedBinding(Queue stockSuppliedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(stockSuppliedQueue).to(exchange).with(STOCK_SUPPLIED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
