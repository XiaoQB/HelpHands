package cn.edu.fudan.service;

import akka.NotUsed;
import cn.edu.fudan.DeleteResult;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.topic;

/**
 * @author fuwuchen
 * @date 2022/5/19 14:06
 */
public interface ServiceService extends Service {
    /**
     * Creates a new service and returns the service ID
     * @return new service
     */
    ServiceCall<ServiceParam, ServiceDTO> add();

    /**
     * Updates the details of an existing service
     * @param id uuid of the service
     * @return updated service
     */
    ServiceCall<ServiceParam, ServiceDTO> updateById(String id);

    /**
     * Increments the stars for the service by one
     * @param id uuid of the service
     * @return updated service
     */
    ServiceCall<ServiceParam, ServiceDTO> rateById(String id);

    /**
     * Gets all the services based on request params
     * @param id uuid of the service
     * @return the service
     */
    ServiceCall<NotUsed, ServiceDTO> findById(String id);

    /**
     * Deletes the specified service
     * @param id uuid of the service
     * @return the deletion result
     */
    ServiceCall<NotUsed, DeleteResult<String>> deleteById(String id);
    /**
     * publish service events to kafka
     * @return service topic
     */
    Topic<ServiceEventPublish> serviceEvent();
    /**
     * describe router
     * @return descriptor
     */
    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return Service.named("service").withCalls(
                Service.restCall(Method.POST, "/services",  this::add),
                Service.restCall(Method.PUT, "/services/:id", this::updateById),
                Service.restCall(Method.PUT, "/services/:id/rate", this::rateById),
                Service.restCall(Method.GET, "/services/:id", this::findById),
                Service.restCall(Method.DELETE, "/services/:id", this::deleteById)
        ).withTopics(
                topic("service-events", this::serviceEvent)
                        .withProperty(KafkaProperties.partitionKeyStrategy(),
                                ServiceEventPublish::getPartitionKey))
                .withAutoAcl(true);
        // @formatter:on
    }
}
