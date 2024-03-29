package cn.edu.fudan;

import akka.NotUsed;
import cn.edu.fudan.domain.consumer.ConsumerDTO;
import cn.edu.fudan.domain.consumer.ConsumerParam;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * @author XiaoQuanbin
 * @date 2022/5/22
 */
public interface ConsumerService extends Service {
    /**
     * Creates a new consumer and returns the consumer ID
     * @return the consumer ID (ServiceComponentDTO)
     */
    ServiceCall<ConsumerParam, ConsumerDTO> add();

    /**
     * Updates the details of an existing consumer
     * @param id service consumer id
     * @return updated ServiceComponentDTO
     */
    ServiceCall<ConsumerParam, ConsumerDTO> updateById(String id);

    /**
     * Gets all the consumers based on request params
     * @return list of ServiceComponentDTO
     */
    ServiceCall<NotUsed, ConsumerDTO> findById(String id);

    /**
     * Deletes the specified consumer
     * @param id service consumer id
     * @return delete status enum
     */
    ServiceCall<NotUsed, DeleteResult<String>> deleteById(String id);

    /**
     * publish consumer events
     * @return consumer topic
     */
    Topic<ConsumerEventPublish> consumerEvent();

    /**
     * route definition
     * @return descriptor
     */
    @Override
    default Descriptor descriptor() {
        return named("consumer").withCalls(
                restCall(Method.POST, "/consumers", this::add),
                restCall(Method.PUT, "/consumers/:id", this::updateById),
                restCall(Method.GET, "/consumers/:id", this::findById),
                restCall(Method.DELETE, "/consumers/:id", this::deleteById)
        ).withTopics(
                topic("consumer-events", this::consumerEvent)
                        .withProperty(KafkaProperties.partitionKeyStrategy(),
                        ConsumerEventPublish::getPartitionKey))
                .withAutoAcl(true);
    }
}
