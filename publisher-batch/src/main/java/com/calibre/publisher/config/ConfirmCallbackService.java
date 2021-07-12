package com.calibre.publisher.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfirmCallbackService implements RabbitTemplate.ConfirmCallback {
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (!ack) {
            log.error("ConfirmCallback error correlationData:{},ack:{},cause={}", correlationData.getId(), ack, cause);
        } else {
            log.info("ConfirmCallback success，correlationData={} ,ack={}", correlationData.getId(), ack);
        }
    }
}
