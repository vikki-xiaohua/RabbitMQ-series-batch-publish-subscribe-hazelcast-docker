package com.calibre.publisher.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface IFxCurrencyRateService {
    void getFxRateBatchAndSend(String currencyPair) throws JsonProcessingException;
}
