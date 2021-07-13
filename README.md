

# rabbitmq-pub-sub

  
  

  

## How to run with docker

  

1. With this [docker-compose.yml](https://github.com/vikki-xiaohua/rabbitmq-pub-sub/blob/main/docker-compose.yml), use command

  

```

docker-compose up

```

  

>  [Docker Compose](https://docs.docker.com/compose/) is a tool for defining and running multi-container Docker applications.

  

2. Or build, link and start your own docker images and containers with the Dockerfile within each project

  

## Main Considerations

  

1, It seems the give FX data APIs are just plain APIs and no advanced usage of these APIs, like Websocket, or Long pool, which can be used for real-time data transfer.

  

So I decided to use scheduled jobs requesting the realtime API with a list of interested currency pairs (*The FX rates of interest are AUDUSD, AUDNZD, AUDHKD, AUDKRW and AUDJPY*) at a fixed rate, as well as providing a controller to be used for accepting a **currency_pair** (e.g. EUR)as the request parameter to monitor the FX REST End Point for changes

  

2, Though usually a separate service(like Redis cluster) as a store/cache layer is beneficial for the whole system availability and maintainability, i.e. Data service and business service separated. And even your business servers are down, the data is still persistent.

  

For a demo purpose, a simple inbuilt classes like ConcurrentHashMap is enough, but as an open-source distributed In-memory data store, Hazelcast now is popular, so I feel it worths a try.

  

> Hazelcast is used for caching objects from third party APIs, as well as for caching messgeIds received from rabbitMQ to prevent potential duplicate comsuming. Use Hazelcast as a database for data you can recover or ignore if lost. Hz gives you many tools to recover the data: [MapStore](https://docs.hazelcast.com/imdg/4.2/data-structures/map.html#loading-and-storing-persistent-data), [Connectors](https://jet-start.sh/docs/api/sources-sinks). I wouldn’t recommend using it when you need strong durability (as in “ACID” database), e.g. for a long-term storage.


3, For RabbitMQ message produce and consume, Manually ack and retry mechanism is used. "error-queue" and "error-batch-queue" are configured for error messages exceeded max retry times.

  ![enter image description here](https://github.com/vikki-xiaohua/rabbitmq-pub-sub/blob/main/image/error-queues.png)  

I also setup a demo project to use [BatchingRabbitTemplate](https://docs.spring.io/spring-amqp/docs/current/api/org/springframework/amqp/rabbit/core/BatchingRabbitTemplate.html) to send messages.

  

But it looks like Batch is not supported as good as single message send and receive until now, less reference material compared with RabbitTemplate

  

#### [Rabbitmq-Batch-Rabbitmq-Publish-Subscribe Demo](https://github.com/vikki-xiaohua/rabbitmq-batch-rabbitmq-publish-subscribe)



4, For generated CSV files, I saved them in the project folder, and provide a scheduled task to delete files older than 10 minutes ago. In production, usually have a separate file server to take care of all the files, I do the "Delete" action mainly for local laptop during development

  

5, In this project, I checked if the throwable (exception/error) is an instance of error, if Yes, I will send out an email since the errors are fatal exceptions in Java.

  

Another idea is to define a set of custom exceptions, which emails should be sent once happened.

  
  

## Some Other Notes

  

### Topic exchanges

Topic exchanges are used in this project

  ![enter image description here](https://github.com/vikki-xiaohua/rabbitmq-pub-sub/blob/main/image/topic-exchange.png)

### HAZELCAST timeToLive vs MaxIdleTime

  

> Both Time-to-Live and Max Idle Seconds may be used simultaneously on the map entries. In that case, the entry is considered expired if at least one of the policies marks it as expired.

  

> Valid values are integers between 0 and Integer.MAX_VALUE. Its default value is 0, which means infinite.

  

[https://docs.hazelcast.com/cloud/map-configurations.html](Both%20Time-to-Live%20and%20Max%20Idle%20Seconds%20may%20be%20used%20simultaneously%20on%20the%20map%20entries.%20In%20that%20case,%20the%20entry%20is%20considered%20expired%20if%20at%20least%20one%20of%20the%20policies%20marks%20it%20as%20expired.)

  

### Consumer retry

  

> When you want to use consumer retry mechanism, you should throw the exceptions, instead of catch them, otherwise the retry won't work. Logs below show that the retry mechanism is working.

```

2021-07-09 16:37:18.867 WARN 31096 --- [ntContainer#0-3] o.s.a.r.r.RejectAndDontRequeueRecoverer : Retries exhausted for message (Body:'[B@639dc86c(byte[40])' MessageProperties [headers={spring_listener_return_correlation=8ee838df-a964-4584-a62d-9b4f9840088c,

```

  

### Rabbitmq publisher confirm

  

```RabbitTemplate.ConfirmCallback```: When messages are successfully or not successfully published into exchange.

  

```RabbitTemplate.ReturnsCallback```: When messages are successfully published an exchange, but not into a queue. E.g. when I bind my queue to a topic exchange with an invalid routing key, my ReturnsCallback is called.

  

### Email service

  

I use Thread.sleep(2000) to prevent the email service from being overloaded. It's better to have an isolated email service in production if we need to send a lot of emails.



### Also  see

> https://github.com/vikki-xiaohua/rabbitmq-batch-rabbitmq-publish-subscribe
