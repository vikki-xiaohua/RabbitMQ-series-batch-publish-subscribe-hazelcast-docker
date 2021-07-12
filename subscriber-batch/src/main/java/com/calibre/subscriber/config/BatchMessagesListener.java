package com.calibre.subscriber.config;

import com.calibre.subscriber.model.FxCurrencyRateCsvRow;
import com.calibre.subscriber.service.ICsvService;
import com.calibre.subscriber.service.IEmailService;
import com.calibre.subscriber.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BatchMessagesListener implements ChannelAwareBatchMessageListener {
    private static ThreadLocal<SimpleDateFormat> dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd_HHmm"));

    private ICsvService csvService;
    private IEmailService emailService;
    private ObjectMapper objectMapper;
    private HazelcastCache cacheClient;

    @Autowired
    public BatchMessagesListener(ICsvService csvService, IEmailService emailService, ObjectMapper objectMapper, HazelcastCache cacheClient) {
        this.csvService = csvService;
        this.emailService = emailService;
        this.objectMapper = objectMapper;
        this.cacheClient = cacheClient;
    }

    @SneakyThrows
    @Override
    public void onMessageBatch(List<Message> messages, Channel channel) {
        log.info("onMessageBatch messages: {}, thread id: {} ", messages, Thread.currentThread().getId());
        if (CollectionUtils.isEmpty(messages)) return;

        Message lastMessage = messages.get(messages.size() - 1);
        String messageId = lastMessage.getMessageProperties().getMessageId();
        long deliveryTag = lastMessage.getMessageProperties().getDeliveryTag();

        try {
            List<FxCurrencyRateCsvRow> toSendList = messages.stream()
                    .filter(a -> ArrayUtils.isNotEmpty(a.getBody()))
                    .map(b -> {
                        try {
                            byte[] messageBody = b.getBody();
                            log.info(new String(messageBody));
                            return objectMapper.readValue(messageBody, FxCurrencyRateCsvRow.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());

            String fileName = Constants.CSV_FILE_PREFIX + dateFormat.get().format(System.currentTimeMillis()) + Constants.CSV_FILE_SUFFIX;

            csvService.createCsvFileAndSendFromBatch(toSendList, fileName);
            cacheClient.putMessage(messageId, true);

            channel.basicAck(deliveryTag, true);
            log.info("onMessageBatch: {} confirmation, and send csv: {}", messages, fileName);

        } catch (Exception | Error throwable) {
            logAndEmail(channel, deliveryTag, throwable);
            throw throwable;
        }
    }

    private void logAndEmail(Channel channel, long deliveryTag, Throwable throwable) {
        log.error("exception | error: {}", Arrays.toString(throwable.getStackTrace()));
        try {
            channel.basicNack(deliveryTag, true, false);
            if (throwable instanceof Error) {
                emailService.sendEmail(Constants.APPLICATION_ERROR_SUBJECT, Constants.APPLICATION_ERROR_BODY + Arrays.toString(throwable.getStackTrace()) + "</p>", null);
            }
        } catch (IOException | MessagingException | InterruptedException ioException) {
            log.error("IOException | MessagingException: {}", Arrays.toString(ioException.getStackTrace()));
        }
    }

}

