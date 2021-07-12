package com.calibre.subscriber.config;

import com.calibre.subscriber.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitConfig {
    @Value("${spring.rabbitmq.host}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.username}")
    private String rabbitUsername;

    @Value("${spring.rabbitmq.password}")
    private String rabbitPassword;

    @Bean(name = "connectionFactory")
    CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(rabbitHost);
        cachingConnectionFactory.setUsername(rabbitUsername);
        cachingConnectionFactory.setPassword(rabbitPassword);
        cachingConnectionFactory.setConnectionTimeout(15000);

        return cachingConnectionFactory;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper;
    }

    @Bean(name = "batchQueue")
    Queue batchQueue() {
        Map<String, Object> params = new HashMap<>();
        params.put("x-dead-letter-exchange", Constants.ERROR_BATCH_EXCHANGE);
        params.put("x-dead-letter-routing-key", Constants.ERROR_BATCH_ROUTING_KEY);

        return QueueBuilder.durable(Constants.TOPIC_BATCH_QUEUE_FX_RATE_API).withArguments(params).build();
    }

    @Bean(name = "batchExchange")
    TopicExchange exchange() {
        return new TopicExchange(Constants.TOPIC_BATCH_EXCHANGE_FX_RATE_API);
    }

    @Bean(name = "batchBinding")
    Binding batchBinding(@Qualifier("batchQueue") Queue queue, @Qualifier("batchExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(Constants.TOPIC_BATCH_FX_RATE_API_ROUTING_KEY);
    }

    @Bean(name = "errorBatchExchange")
    public DirectExchange errorBatchExchange() {
        return new DirectExchange(Constants.ERROR_BATCH_EXCHANGE, true, false);
    }

    @Bean(name = "errorBatchQueue")
    public Queue errorBatchQueue() {
        return new Queue(Constants.ERROR_BATCH_QUEUE, true);
    }

    @Bean(name = "errorBatchBinding")
    public Binding errorBatchBinding(@Qualifier("errorBatchQueue") Queue errorQueue, @Qualifier("errorBatchExchange") DirectExchange errorExchange) {
        return BindingBuilder.bind(errorQueue).to(errorExchange).with(Constants.ERROR_BATCH_ROUTING_KEY);
    }

}
