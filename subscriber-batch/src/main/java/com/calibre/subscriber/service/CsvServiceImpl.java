package com.calibre.subscriber.service;

import com.calibre.subscriber.model.FxCurrencyRateCsvRow;
import com.calibre.subscriber.model.FxRateCsvHeader;
import com.calibre.subscriber.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.mail.MessagingException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Slf4j
@Service
public class CsvServiceImpl implements ICsvService {
    private IEmailService emailService;

    @Autowired
    public CsvServiceImpl(IEmailService iEmailService) {
        this.emailService = iEmailService;
    }

    @Async("csv-service-taskExecutor")
    @Override
    public void createCsvFileAndSendFromBatch(List<FxCurrencyRateCsvRow> fxCurrencyRateCsvRowList, String fileName) throws IOException, MessagingException, InterruptedException {
        log.info("createCsvFileAndSendFromList fxCurrencyRateCsvRowList: {},fileName:{},thread id:{}", fxCurrencyRateCsvRowList, fileName, Thread.currentThread().getId());
        if (CollectionUtils.isEmpty(fxCurrencyRateCsvRowList)) return;

        try (Writer writer = new FileWriter(fileName);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(FxRateCsvHeader.class))
        ) {

            for (FxCurrencyRateCsvRow fxCurrencyRateCsvRow : fxCurrencyRateCsvRowList) {
                csvPrinter.printRecord(fxCurrencyRateCsvRow.getForex(), fxCurrencyRateCsvRow.getValue());
            }
        }

        emailService.sendEmail(Constants.CSV_ATTACHMENT_BATCH_SUBJECT, Constants.CSV_ATTACHMENT_MESSAGE, new String[]{fileName});
    }

}
