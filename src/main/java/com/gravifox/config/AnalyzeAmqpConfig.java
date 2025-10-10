package com.gravifox.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnalyzeAmqpConfig {

    @Value("${analyze.exchange:analyze.exchange}")
    private String analyzeExchangeName;

    @Value("${analyze.instance-id:local}")
    private String instanceId;

    private String sanitize(String s) {
        return s == null ? "local" : s.replaceAll("[^A-Za-z0-9_.-]", "-");
    }

    @Bean
    public TopicExchange analyzeExchange() {
        // durable topic exchange
        return new TopicExchange(analyzeExchangeName, true, false);
    }

    @Bean
    public Queue analyzeBridgeQueue() {
        // 인스턴스 전용 durable 큐
        String queueName = "analyze.bridge." + sanitize(instanceId);
        return new Queue(queueName, true);
    }

    @Bean
    public Declarables analyzeBindings(TopicExchange analyzeExchange, Queue analyzeBridgeQueue) {
        Binding progress = BindingBuilder
                .bind(analyzeBridgeQueue)
                .to(analyzeExchange)
                .with("analyze.progress.*");

        Binding result = BindingBuilder
                .bind(analyzeBridgeQueue)
                .to(analyzeExchange)
                .with("analyze.result.*");

        Binding failed = BindingBuilder
                .bind(analyzeBridgeQueue)
                .to(analyzeExchange)
                .with("analyze.failed.*");

        return new Declarables(progress, result, failed);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory cf,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(converter);
        return template;
    }
}
