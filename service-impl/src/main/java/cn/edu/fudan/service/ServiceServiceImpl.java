package cn.edu.fudan.service;

import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.japi.Pair;
import cn.edu.fudan.DeleteResult;
import cn.edu.fudan.DeleteStatus;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import java.time.Duration;
import java.util.UUID;

/**
* @author fuwuchen
* @date 2022/7/15 15:26
*/
public class ServiceServiceImpl implements ServiceService {

    private final PersistentEntityRegistry persistentEntityRegistry;

    private final Duration askTimeout = Duration.ofSeconds(5);
    private final ClusterSharding clusterSharding;

    @Inject
    public ServiceServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
                               ClusterSharding clusterSharding) {
        this.clusterSharding = clusterSharding;
        // The persistent entity registry is only required to build an event stream for the TopicProducer
        this.persistentEntityRegistry = persistentEntityRegistry;
        // register the Aggregate as a sharded entity
        this.clusterSharding.init(
                Entity.of(
                        ServiceEntity.ENTITY_TYPE_KEY,
                        ServiceEntity::create
                )
        );
    }
    /**
     * Creates a new service and returns the service ID
     *
     * @return new service
     */
    @Override
    public ServiceCall<ServiceParam, ServiceDTO> add() {
        return request -> {
            request.setId(UUID.randomUUID().toString());
            // Look up the aggregate instance for the given ID.
            EntityRef<ServiceCommand> ref = entityRefFor(request.getId());
            return ref.
                    <ServiceCommand.Confirmation>ask(
                            replyTo -> new ServiceCommand.Add(request, replyTo), askTimeout)
                    .thenApplyAsync(this::handleConfirmation)
                    .thenApplyAsync(accepted -> (ServiceDTO) accepted.get());
        };
    }

    private EntityRef<ServiceCommand> entityRefFor(String id) {
        return clusterSharding.entityRefFor(ServiceEntity.ENTITY_TYPE_KEY, id);
    }

    /**
     * Updates the details of an existing service
     *
     * @param id uuid of the service
     * @return updated service
     */
    @Override
    public ServiceCall<ServiceParam, ServiceDTO> updateById(String id) {
        return request -> {
            request.setId(id);
            // Look up the aggregate instance for the given ID.
            EntityRef<ServiceCommand> ref = entityRefFor(id);
            return ref.
                    <ServiceCommand.Confirmation>ask(
                            replyTo -> new ServiceCommand.UpdateById(request, replyTo), askTimeout)
                    .thenApplyAsync(this::handleConfirmation)
                    .thenApplyAsync(accepted -> (ServiceDTO) accepted.get());
        };
    }

    /**
     * Increments the stars for the service by one
     *
     * @param id uuid of the service
     * @return updated service
     */
    @Override
    public ServiceCall<ServiceParam, ServiceDTO> rateById(String id) {
        return null;
    }

    /**
     * Gets all the services based on request params
     *
     * @param id uuid of the service
     * @return the service
     */
    @Override
    public ServiceCall<NotUsed, ServiceDTO> findById(String id) {
        return request -> {
            // Look up the aggregate instance for the given ID.
            EntityRef<ServiceCommand> ref = entityRefFor(id);
            return ref.
                    <ServiceCommand.Confirmation>ask(
                            replyTo -> new ServiceCommand.FindById(id, replyTo), askTimeout)
                    .thenApplyAsync(this::handleConfirmation)
                    .thenApplyAsync(accepted -> (ServiceDTO) accepted.get());
        };
    }

    /**
     * Deletes the specified service
     *
     * @param id uuid of the service
     * @return the deletion result
     */
    @Override
    public ServiceCall<NotUsed, DeleteResult<String>> deleteById(String id) {
        return request -> {
            // Look up the aggregate instance for the given ID.
            EntityRef<ServiceCommand> ref = entityRefFor(id);
            return ref.
                    <ServiceCommand.Confirmation>ask(
                            replyTo -> new ServiceCommand.DeleteById(id, replyTo), askTimeout)
                    .thenApplyAsync(this::handleConfirmation)
                    .thenApplyAsync(accepted -> new DeleteResult<>((DeleteStatus) accepted.get(), id));
        };
    }

    /**
     * publish service events to kafka
     *
     * @return service topic
     */
    @Override
    public Topic<ServiceEventPublish> serviceEvent() {
        return TopicProducer.taggedStreamWithOffset(ServiceEvent.TAG.allTags(), (tag, offset) ->
                // Load the event stream for the passed in shard tag
                persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {
                    ServiceEventPublish eventToPublish;
                    ServiceEvent event = eventAndOffset.first();
                    if (event instanceof ServiceEvent.ServiceAdded) {
                        ServiceEvent.ServiceAdded messageChanged = (ServiceEvent.ServiceAdded) event;
                        eventToPublish = new ServiceEventPublish.ServiceAdded(
                                messageChanged.getServiceDTO(), messageChanged.getEventTime()
                        );
                    } else if (event instanceof ServiceEvent.ServiceUpdated) {
                        ServiceEvent.ServiceUpdated messageChanged = (ServiceEvent.ServiceUpdated) event;
                        eventToPublish = new ServiceEventPublish.ServiceUpdated(
                                messageChanged.getServiceDTO(), messageChanged.getEventTime()
                        );
                    } else if (event instanceof ServiceEvent.ServiceDeleted) {
                        ServiceEvent.ServiceDeleted messageChanged = (ServiceEvent.ServiceDeleted) event;
                        eventToPublish = new ServiceEventPublish.ServiceDeleted(
                                messageChanged.getServiceId(), messageChanged.getEventTime()
                        );
                    } else {
                        throw new IllegalArgumentException("Unknown event: " + event);
                    }

                    // We return a pair of the translated event, and its offset, so that
                    // Lagom can track which offsets have been published.
                    return Pair.create(eventToPublish, eventAndOffset.second());
                })
        );
    }

    /**
     * Try to convert Confirmation to an Accepted
     *
     * @throws BadRequest if Confirmation is a Rejected
     */
    private ServiceCommand.Accepted handleConfirmation(ServiceCommand.Confirmation confirmation) {
        if (confirmation instanceof ServiceCommand.Accepted) {
            return (ServiceCommand.Accepted) confirmation;
        }

        ServiceCommand.Rejected rejected = (ServiceCommand.Rejected) confirmation;
        throw new BadRequest(rejected.getReason());
    }
}
