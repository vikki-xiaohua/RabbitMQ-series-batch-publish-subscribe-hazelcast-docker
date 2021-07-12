package com.calibre.subscriber.config;

import com.calibre.subscriber.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

@Slf4j
@Component
public class ScheduledLocalFileDeleteTask {
    @Scheduled(fixedRate = Constants.SCHEDULED_FILE_DELETE_FIXED_RATE_MILLISECONDS,
            initialDelay = Constants.SCHEDULED_FILE_DELETE_INITIAL_DELAY_MILLISECONDS)
    public void deleteLocalFiles() {
        File fileList = new File(".");
        if (ObjectUtils.isEmpty(fileList)) return;

        File[] fileArray = fileList.listFiles((d, f) -> f.toLowerCase().endsWith(Constants.CSV_FILE_SUFFIX));
        if (ArrayUtils.isEmpty(fileArray)) return;

        log.info("fileList with csv extension: {} ", Arrays.toString(fileArray));

        for (File filePath : fileArray) {
            long diff = new Date().getTime() - filePath.lastModified();

            if (diff > Constants.SCHEDULED_FILE_DELETE_FIXED_RATE_MILLISECONDS) {
                if (filePath.exists() && !filePath.isDirectory()) {
                    boolean success = filePath.delete();
                    log.info("file: {} deleted?: {} ", filePath, success);
                }
            }
        }
    }
}