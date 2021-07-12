package com.calibre.publisher.config;

import com.calibre.publisher.util.Constants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.nio.charset.StandardCharsets;
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

    @Autowired
    private ConfirmCallbackService confirmCallbackService;

    @Autowired
    private ReturnCallbackService returnCallbackService;

    @Bean(name = "connectionFactory")
    CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(rabbitHost);
        cachingConnectionFactory.setUsername(rabbitUsername);
        cachingConnectionFactory.setPassword(rabbitPassword);
        cachingConnectionFactory.setConnectionTimeout(15000);
        cachingConnectionFactory.setPublisherReturns(true);
        cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);

        return cachingConnectionFactory;
    }

    @Bean(name = "batchingRabbitTemplate")
    @Primary
    public BatchingRabbitTemplate batchRabbitTemplate(@Qualifier("connectionFactory") ConnectionFactory connectionFactory,
                                                      BatchingStrategy batchingStrategy) {
        TaskScheduler taskScheduler = new ConcurrentTaskScheduler();
        BatchingRabbitTemplate batchTemplate = new BatchingRabbitTemplate(batchingStrategy, taskScheduler);
        batchTemplate.setConnectionFactory(connectionFactory);
        batchTemplate.setExchange(Constants.TOPIC_BATCH_EXCHANGE_FX_RATE_API);

        batchTemplate.setMessageConverter(jsonMessageConverter());
        batchTemplate.setEncoding(StandardCharsets.UTF_8.name());

        ThreadPoolTaskScheduler taskExecutor = new ThreadPoolTaskScheduler();
        taskExecutor.setPoolSize(2);
        batchTemplate.setTaskExecutor(taskExecutor);

        batchTemplate.setConfirmCallback(confirmCallbackService);
        batchTemplate.setReturnsCallback(returnCallbackService);
        batchTemplate.setMandatory(true);

        return batchTemplate;
    }

    @Bean
    public BatchingStrategy batchingStrategy() {
        return new SimpleBatchingStrategy(Constants.BATCHING_STRATEGY_BATCH_SIZE,
                Constants.BATCHING_STRATEGY_BUFFER_LIMIT, Constants.BATCHING_STRATEGY_TIMEOUT);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setDefaultCharset(StandardCharsets.UTF_8.name());
        return converter;
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
