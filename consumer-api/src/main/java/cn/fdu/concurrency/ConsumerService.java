package cn.fdu.concurrency;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import static com.lightbend.lagom.javadsl.api.Service.named;


import static com.lightbend.lagom.javadsl.api.Service.pathCall;
import static com.lightbend.lagom.javadsl.api.Service.topic;

/**
 * @author XiaoQuanbin
 * @date 2022/5/19
 */
public interface ConsumerService extends Service{

    ServiceCall<NotUsed, String> consumer(String id);

    /**
     * This gets published to Kafka.
     */
    Topic<ConsumerEvent> helloEvents();
    @Override
    default Descriptor descriptor(){
        // @formatter:off
        return named("consumers").withCalls(
                pathCall("/consumer/:id",  this::consumer)
        ).withTopics(
                topic("hello-events", this::helloEvents)
                        // Kafka partitions messages, messages within the same partition will
                        // be delivered in order, to ensure that all messages for the same user
                        // go to the same partition (and hence are delivered in order with respect
                        // to that user), we configure a partition key strategy that extracts the
                        // name as the partition key.
                        .withProperty(KafkaProperties.partitionKeyStrategy(), ConsumerEvent::getName)
        ).withAutoAcl(true);
        // @formatter:on

    }
}
