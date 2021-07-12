package com.calibre.publisher.config;


import com.calibre.publisher.model.FxCurrencyRateCsvRow;
import com.calibre.publisher.util.Constants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class RabbitPublisher {
    private BatchingRabbitTemplate batchingRabbitTemplate;
    private ObjectMapper objectMapper;

    @Autowired
    public RabbitPublisher(@Qualifier("batchingRabbitTemplate") BatchingRabbitTemplate batchingRabbitTemplate,
                           ObjectMapper objectMapper) {
        this.batchingRabbitTemplate = batchingRabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendBatch(FxCurrencyRateCsvRow fxRateCsvRow) throws JsonProcessingException {
        log.info("sendBatch fxRateCsvRow:{}", fxRateCsvRow);

        Message message;

        String json = objectMapper.writeValueAsString(fxRateCsvRow);

        long timeStamp = System.currentTimeMillis();
        String messageId = timeStamp + "-" + UUID.randomUUID();

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentEncoding(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setMessageId(messageId);
        messageProperties.setHeader(AmqpHeaders.DELIVERY_TAG, timeStamp);
        messageProperties.setHeader(AmqpHeaders.CORRELATION_ID, messageId);
        messageProperties.setHeader("objectType", "FxCurrencyRateCsvRow.class");

        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(messageId);

        message = new Message(json.getBytes(), messageProperties);
        log.info("sendBatch message:{}", message);

        batchingRabbitTemplate.convertAndSend(Constants.TOPIC_BATCH_EXCHANGE_FX_RATE_API,
                "topic.batch.fx.rate.routing.key.1", message, correlationData);
    }

}
