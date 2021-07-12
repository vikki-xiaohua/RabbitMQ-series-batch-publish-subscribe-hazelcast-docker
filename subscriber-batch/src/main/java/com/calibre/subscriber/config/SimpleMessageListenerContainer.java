package com.calibre.subscriber.config;

import com.calibre.subscriber.util.Constants;
import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.stereotype.Component;

@Component
public class SimpleMessageListenerContainer {

    @Autowired
    BatchMessagesListener batchMessagesListener;

    @Autowired
    ConnectionFactory connectionFactory;


    @Bean
    public SimpleRabbitListenerContainerFactory listenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setAdviceChain(new Advice[]{retries()});

        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retries() {
        return RetryInterceptorBuilder.stateless().maxAttempts(3)
                .backOffOptions(3000,
                        2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer()).build();
    }


    @Bean
    public org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer messageListenerContainer
            (BatchMessagesListener batchMessagesListener, ConnectionFactory connectionFactory, SimpleRabbitListenerContainerFactory listenerContainerFactory) {

        org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer container = listenerContainerFactory.createListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(Constants.TOPIC_BATCH_QUEUE_FX_RATE_API);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setBatchingStrategy(batchingStrategy());
        container.setBatchSize(Constants.BATCHING_STRATEGY_BATCH_SIZE);
        container.setConsumerBatchEnabled(true);
        container.setDeBatchingEnabled(true);

        container.setMessageListener(batchMessagesListener);

        return container;
    }

    @Bean
    public BatchingStrategy batchingStrategy() {
        BatchingStrategy batchingStrategy = new SimpleBatchingStrategy(Constants.BATCHING_STRATEGY_BATCH_SIZE,
                Constants.BATCHING_STRATEGY_BUFFER_LIMIT, Constants.BATCHING_STRATEGY_TIMEOUT);

        return batchingStrategy;
    }

}
