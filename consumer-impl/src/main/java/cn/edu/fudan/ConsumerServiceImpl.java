package cn.edu.fudan;

import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.japi.Pair;
import cn.edu.fudan.domain.consumer.ConsumerDTO;
import cn.edu.fudan.domain.consumer.ConsumerParam;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import javax.inject.Inject;
import java.time.Duration;
import java.util.UUID;

/**
 * @author XiaoQuanbin
 * @date 2022/5/23
 */
public class ConsumerServiceImpl implements ConsumerService {

    private final PersistentEntityRegistry persistentEntityRegistry;

    private final CassandraSession session;

    private final Duration askTimeout = Duration.ofSeconds(5);
    private ClusterSharding clusterSharding;

    @Inject
    public ConsumerServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
                               ClusterSharding clusterSharding,
                               CassandraSession session,
                               ReadSide readSide){
        this.clusterSharding = clusterSharding;
        // The persistent entity registry is only required to build an event stream for the TopicProducer
        this.persistentEntityRegistry = persistentEntityRegistry;

        // register the Aggregate as a sharded entity
        this.clusterSharding.init(
                Entity.of(
                        ConsumerEntity.ENTITY_TYPE_KEY,
                        ConsumerEntity::create
                )
        );
        this.session = session;
        readSide.register(ConsumerEventProcessor.class);
    }

    /**
     * Creates a new consumer and returns the consumer ID
     *
     * @return the consumer ID (ServiceComponentDTO)
     */
    @Override
    public ServiceCall<ConsumerParam, ConsumerDTO> add() {
        return request -> {
            if (request.getId() == null) {
                request.setId(UUID.randomUUID().toString());
            }
            // Look up the aggregate instance for the given ID.
            EntityRef<ConsumerCommand> ref = entityRefFor(request.getId());
            return ref.
                    <ConsumerCommand.Confirmation>ask(
                    replyTo -> new ConsumerCommand.Add(request, replyTo), askTimeout)
                    .thenApply(this::handleConfirmation)
                    .thenApply(accepted -> (ConsumerDTO) accepted.get());
        };
    }

    private EntityRef<ConsumerCommand> entityRefFor(String id) {
        return clusterSharding.entityRefFor(ConsumerEntity.ENTITY_TYPE_KEY, id);
    }

    /**
     * Updates the details of an existing consumer
     *
     * @param id service consumer id
     * @return updated ServiceComponentDTO
     */
    @Override
    public ServiceCall<ConsumerParam, ConsumerDTO> updateById(String id) {
        return request -> {
            request.setId(id);
            EntityRef<ConsumerCommand> ref = entityRefFor(id);
            return ref.
                    <ConsumerCommand.Confirmation>ask(
                            replyTo -> new ConsumerCommand.UpdateById(request, replyTo), askTimeout)
                    .thenApply(this::handleConfirmation)
                    .thenApply(accepted -> (ConsumerDTO) accepted.get());
        };
    }

    /**
     * Gets all the consumers based on request params
     *
     * @param id consumer id
     * @return list of ServiceComponentDTO
     */
    @Override
    public ServiceCall<NotUsed, ConsumerDTO> findById(String id) {
        return request -> {
            EntityRef<ConsumerCommand> ref = entityRefFor(id);
            return ref.
                    <ConsumerCommand.Confirmation>ask(
                            replyTo -> new ConsumerCommand.FindById(id, replyTo), askTimeout)
                    .thenApply(this::handleConfirmation)
                    .thenApply(accepted -> (ConsumerDTO) accepted.get());
        };
    }

    /**
     * Deletes the specified consumer
     *
     * @param id service consumer id
     * @return delete status enum
     */
    @Override
    public ServiceCall<NotUsed, DeleteResult<String>> deleteById(String id) {
        return request -> {
            EntityRef<ConsumerCommand> ref = entityRefFor(id);
            return ref.
                    <ConsumerCommand.Confirmation>ask(
                            replyTo -> new ConsumerCommand.DeleteById(id, replyTo),  askTimeout)
                    .thenApply(this::handleConfirmation)
                    .thenApply(accepted -> new DeleteResult<>((DeleteStatus)accepted.get() ,id));
        };
    }

    /**
     * publish consumer events
     *
     * @return consumer topic
     */
    @Override
    public Topic<ConsumerEventPublish> consumerEvent() {
        return TopicProducer.taggedStreamWithOffset(ConsumerEvent.TAG.allTags(), (tag, offset) ->
                // Load the event stream for the passed in shard tag
                persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {
                    ConsumerEventPublish eventToPublish;
                    ConsumerEvent event = eventAndOffset.first();
                    if (event instanceof ConsumerEvent.ConsumerAdded) {
                        ConsumerEvent.ConsumerAdded messageChanged = (ConsumerEvent.ConsumerAdded) event;
                        eventToPublish = new ConsumerEventPublish.ConsumerAdded(
                                messageChanged.getConsumerDTO(), messageChanged.getEventTime()
                        );
                    } else if (event instanceof ConsumerEvent.ConsumerUpdated) {
                        ConsumerEvent.ConsumerUpdated messageChanged = (ConsumerEvent.ConsumerUpdated) event;
                        eventToPublish = new ConsumerEventPublish.ConsumerUpdated(
                                messageChanged.getConsumerDTO(), messageChanged.getEventTime()
                        );
                    } else if (event instanceof ConsumerEvent.ConsumerDeleted) {
                        ConsumerEvent.ConsumerDeleted messageChanged = (ConsumerEvent.ConsumerDeleted) event;
                        eventToPublish = new ConsumerEventPublish.ConsumerDeleted(
                                messageChanged.getConsumerId(), messageChanged.getEventTime()
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
    private ConsumerCommand.Accepted handleConfirmation(ConsumerCommand.Confirmation confirmation) {
        if (confirmation instanceof ConsumerCommand.Accepted) {
            return (ConsumerCommand.Accepted) confirmation;
        }

        ConsumerCommand.Rejected rejected = (ConsumerCommand.Rejected) confirmation;
        throw new BadRequest(rejected.getReason());
    }
}
