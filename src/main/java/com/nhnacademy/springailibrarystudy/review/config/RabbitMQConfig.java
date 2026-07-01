package com.nhnacademy.springailibrarystudy.review.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "nhnacademy-library-exchange";
    public static final String ROUTING_KEY = "review.summary";

    @Value("${rabbitmq.queue.review-summary}")
    private String queueName;

    @Bean
    public Queue reviewSummaryQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public DirectExchange reviewExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Binding reviewBinding(Queue reviewSummaryQueue, DirectExchange reviewExchange) {
        return BindingBuilder.bind(reviewSummaryQueue).to(reviewExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}