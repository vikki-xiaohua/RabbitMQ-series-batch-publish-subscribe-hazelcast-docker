package com.calibre.subscriber.service;

import com.calibre.subscriber.model.FxCurrencyRateCsvRow;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface ICsvService {
    void createCsvFileAndSendFromBatch(List<FxCurrencyRateCsvRow> toSendList, String fileName) throws IOException, MessagingException, InterruptedException;
}
