package com.calibre.publisher.config;

import com.calibre.publisher.service.IEmailService;
import com.calibre.publisher.service.IFxCurrencyRateService;
import com.calibre.publisher.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ScheduledDataTasks {
    public static final String FOREX_ENDPOINT_CURRENCY_PAIR_LIST = "forex.endpoint.currency-pair-list";
    private List<String> currencyPairList;
    private Environment env;
    private IFxCurrencyRateService fxCurrencyRateService;
    private IEmailService emailService;

    private RabbitPublisher rabbitPublisher;

    @Autowired
    public ScheduledDataTasks(IFxCurrencyRateService fxCurrencyRateService, IEmailService emailService,
                              Environment env, RabbitPublisher rabbitPublisher) {
        this.fxCurrencyRateService = fxCurrencyRateService;
        this.emailService = emailService;
        this.env = env;
        this.rabbitPublisher = rabbitPublisher;
    }

    @Scheduled(fixedRate = Constants.SCHEDULED_DATA_REQUEST_FIXED_RATE_MILLISECONDS,
            initialDelay = Constants.SCHEDULED_DATA_REQUEST_RATE_INITIAL_DELAY_MILLISECONDS)
    public void scheduleTaskWithFixedRate() {
        try {
            currencyPairList = env.getProperty(FOREX_ENDPOINT_CURRENCY_PAIR_LIST, List.class);
            if (CollectionUtils.isEmpty(currencyPairList)) return;

            for (String currencyPair : currencyPairList) {
                fxCurrencyRateService.getFxRateBatchAndSend(currencyPair);
            }

        } catch (Exception | Error throwable) {
            log.error("exception | error: {}", Arrays.toString(throwable.getStackTrace()));

            if (throwable instanceof Error) {
                emailService.sendEmail(Constants.APPLICATION_ERROR_SUBJECT, Constants.APPLICATION_ERROR_BODY + Arrays.toString(throwable.getStackTrace()) + "</p>", null);
            }
        }
    }

}