package com.epicerie.supplier.config;

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
    public static final String STOCK_LOW_QUEUE = "q.supplier.stock.low";
    public static final String STOCK_LOW_ROUTING_KEY = "product.stock.low";
    public static final String STOCK_SUPPLIED_ROUTING_KEY = "supplier.stock.supplied";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue stockLowQueue() {
        return new Queue(STOCK_LOW_QUEUE);
    }

    @Bean
    public Binding stockLowBinding(Queue stockLowQueue, TopicExchange exchange) {
        return BindingBuilder.bind(stockLowQueue).to(exchange).with(STOCK_LOW_ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
