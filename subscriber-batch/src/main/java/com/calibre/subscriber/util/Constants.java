package com.calibre.subscriber.util;

public class Constants {
    public static final String TOPIC_BATCH_EXCHANGE_FX_RATE_API = "topic-batch-exchange-fx-rate";
    public static final String TOPIC_BATCH_QUEUE_FX_RATE_API = "topic-batch-queue-fx-rate";
    public static final String TOPIC_BATCH_FX_RATE_API_ROUTING_KEY = "topic.batch.fx.rate.routing.key.#";

    public static final String CSV_ATTACHMENT_BATCH_SUBJECT = "Updated Forex Currency Rates Batch";
    public static final String CSV_ATTACHMENT_MESSAGE = "<p>Attachment includes the Latest Forex Currency Rates Changes Detail<br>";

    public static final String APPLICATION_ERROR_SUBJECT = "Subscriber Application Error!";
    public static final String APPLICATION_ERROR_BODY = "<p>Subscriber Application Error Detail:<br>";

    public static final String CSV_FILE_PREFIX = "obsval_";
    public static final String CSV_FILE_SUFFIX = ".csv";

    public static final int SCHEDULED_FILE_DELETE_FIXED_RATE_MILLISECONDS = 10 * 60 * 1000;
    public static final int SCHEDULED_FILE_DELETE_INITIAL_DELAY_MILLISECONDS = 3000;

    public static final int BATCHING_STRATEGY_BATCH_SIZE = 5;
    public static final int BATCHING_STRATEGY_BUFFER_LIMIT = 10 * 1024 * 1024;
    public static final int BATCHING_STRATEGY_TIMEOUT = 10 * 60 * 1000;

    public static final String ERROR_BATCH_EXCHANGE = "error-batch-exchange";
    public static final String ERROR_BATCH_QUEUE = "error-batch-queue";
    public static final String ERROR_BATCH_ROUTING_KEY = "error-batch-routing-key";

    public static final String HAZELCAST_CACHE_KEY_PREFIX = "fx-rate-rabbit-message-cache";
}
