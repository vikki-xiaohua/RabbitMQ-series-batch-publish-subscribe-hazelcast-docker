
# rabbitmq-batch-pub-sub


##  Batch Publish and Subscribe

[Batch Publishing](https://www.rabbitmq.com/publishers.html) This strategy involves publishing batches of messages and awaiting for the entire batch to be confirmed. Retries are performed on batches.

In your codes, you can still send message on by one independently, but these messages canbe received  in a bulk (batch size)

**Publish**
```
batchingRabbitTemplate.convertAndSend()
```

**Subscribe**
```
2021-07-10 00:44:48,391 INFO [messageListenerContainer-2] subscriber.config.BatchMessagesListener: {"forex":"EUR","value":1.626021238826E12}

2021-07-10 00:44:48,394 INFO [messageListenerContainer-2] subscriber.config.BatchMessagesListener: {"forex":"EUR","value":1.626021239625E12}

2021-07-10 00:44:48,395 INFO [messageListenerContainer-2] subscriber.config.BatchMessagesListener: {"forex":"EUR","value":1.626021240512E12}

2021-07-10 00:44:48,396 INFO [messageListenerContainer-2] subscriber.config.BatchMessagesListener: {"forex":"EUR","value":1.626021241073E12}

2021-07-10 00:44:48,399 INFO [messageListenerContainer-2] subscriber.config.BatchMessagesListener: {"forex":"EUR","value":1.626021241528E12}
```

## Batch on Publisher  vs Batch on Subscriber

It looks like batch consumption is not as easy as non-batch message consumption. the following strategies are tried:

###  1, BatchingRabbitTemplate.receive().

I find it's not friendly to handle  manually ack via Channel  ([How to get Channel object in Spring boot AMQP](https://stackoverflow.com/questions/63115809/how-to-get-channel-object-in-spring-boot-amqp-and-create-a-exchange-of-type-x-c))


###  2, implements ChannelAwareBatchMessageListener

This is the one used in this project


###  3, @RabbitListener

Unfortunately, I didn't figure out a way to make it work with batch


## Todo
Load test to compare the throughput between  Batch on Publisher (send a list directly)  vs Batch on Subscriber


### Also  see

> https://github.com/vikki-xiaohua/rabbitmq-publish-subscribe-hazelcast-docker


### It's forbidden to use these codes for a commercial product
