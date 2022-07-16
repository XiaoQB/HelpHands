package cn.edu.fudan.provider;

import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import cn.edu.fudan.DeleteResult;
import cn.edu.fudan.DeleteStatus;
import cn.edu.fudan.provider.api.ProviderDTO;
import cn.edu.fudan.provider.api.ProviderParam;
import cn.edu.fudan.provider.api.ProviderService;
import cn.edu.fudan.provider.domain.ProviderEventPublish;
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
import java.util.concurrent.CompletableFuture;

/**
 * @author fuwuchen
 * @date 2022/5/19 18:11
 */
public class ProviderServiceImpl implements ProviderService {

    private final PersistentEntityRegistry persistentEntityRegistry;

    private final CassandraSession cassandraSession;

    private final Duration askTimeout = Duration.ofSeconds(5);
    private final ClusterSharding clusterSharding;

    @Inject
    public ProviderServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
                               CassandraSession cassandraSession,
                               ClusterSharding clusterSharding,
                               ReadSide readSide) {
        this.cassandraSession = cassandraSession;
        this.clusterSharding = clusterSharding;
        // The persistent entity registry is only required to build an event stream for the TopicProducer
        this.persistentEntityRegistry = persistentEntityRegistry;
        readSide.register(ProviderEventProcessor.class);
        // register the Aggregate as a sharded entity
        this.clusterSharding.init(
                Entity.of(
                        ProviderEntity.ENTITY_TYPE_KEY,
                        ProviderEntity::create
                )
        );
    }

    /**
     * Creates a new provider and returns the provider ID
     *
     * @return the provider ID (ServiceComponentDTO)
     */
    @Override
    public ServiceCall<ProviderParam, ProviderDTO> add() {
        return request -> {
            if (request.getId() == null) {
                request.setId(UUID.randomUUID().toString());
            }
            // Look up the aggregate instance for the given ID.
            EntityRef<ProviderCommand> ref = entityRefFor(request.getId());
            return ref.
                    <ProviderCommand.Confirmation>ask(
                            replyTo -> new ProviderCommand.Add(request, replyTo), askTimeout)
                    .thenApply(this::handleConfirmation)
                    .thenApply(accepted -> (ProviderDTO) accepted.get());
        };
    }

    private EntityRef<ProviderCommand> entityRefFor(String id) {
        return clusterSharding.entityRefFor(ProviderEntity.ENTITY_TYPE_KEY, id);
    }

    /**
     * Updates the details of an existing provider
     *
     * @param id service provider id
     * @return updated ServiceComponentDTO
     */
    @Override
    public ServiceCall<ProviderParam, ProviderDTO> updateById(String id) {
        return request -> {
            request.setId(id);
            // Look up the aggregate instance for the given ID.
            EntityRef<ProviderCommand> ref = entityRefFor(id);
            return ref.
                    <ProviderCommand.Confirmation>ask(
                    replyTo -> new ProviderCommand.UpdateById(request, replyTo), askTimeout)
                    .thenApply(this::handleConfirmation)
                    .thenApply(accepted -> (ProviderDTO) accepted.get());
        };
    }

    /**
     * Adds to the latest ratings for the provider.
     *
     * @param id service provider id
     * @return updated ServiceComponentDTO
     */
    @Override
    public ServiceCall<ProviderParam, ProviderDTO> rateById(String id) {
        return null;
    }

    /**
     * Gets all the providers based on request params
     *
     * @return list of ServiceComponentDTO
     */
    @Override
    public ServiceCall<NotUsed, ProviderDTO> findById(String id) {
        return request -> {
            // Look up the aggregate instance for the given ID.
            EntityRef<ProviderCommand> ref = entityRefFor(id);
            return ref.
                    <ProviderCommand.Confirmation>ask(
                    replyTo -> new ProviderCommand.FindById(id, replyTo), askTimeout)
                    .thenApply(this::handleConfirmation)
                    .thenApply(accepted -> (ProviderDTO) accepted.get());
        };
    }

    /**
     * Gets all the providers
     *
     * @return list of providers
     */
    @Override
    public ServiceCall<NotUsed, Source<ProviderDTO, ?>> findAll() {
        return request -> {
            Source<ProviderDTO, ?> summaries =
                    cassandraSession
                            .select(String.format("SELECT id, name, mobile, since, rating FROM %s;",
                                    ProviderConfig.TABLE_NAME))
                            .map(row -> {
                                System.out.println(row);
                                return new ProviderDTO(
                                        row.getString("id"),
                                        row.getString("name"),
                                        row.getString("mobile"),
                                        row.getLong("since"),
                                        row.getFloat("rating")
                                );
                            })
                            ;

            return CompletableFuture.completedFuture(summaries);
        };
    }

    /**
     * Deletes the specified provider
     *
     * @param id service provider id
     * @return delete status enum
     */
    @Override
    public ServiceCall<NotUsed, DeleteResult<String>> deleteById(String id) {
        return request -> {
            // Look up the aggregate instance for the given ID.
            EntityRef<ProviderCommand> ref = entityRefFor(id);
            return ref.
                    <ProviderCommand.Confirmation>ask(
                    replyTo -> new ProviderCommand.DeleteById(id, replyTo), askTimeout)
                    .thenApply(this::handleConfirmation)
                    .thenApply(accepted -> new DeleteResult<>((DeleteStatus) accepted.get(), id));
        };
    }

    /**
     * publish provider events
     *
     * @return provider topic
     */
    @Override
    public Topic<ProviderEventPublish> providerEvent() {
        return TopicProducer.taggedStreamWithOffset(ProviderEvent.TAG.allTags(), (tag, offset) ->
                // Load the event stream for the passed in shard tag
                persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {
                    ProviderEventPublish eventToPublish;
                    ProviderEvent event = eventAndOffset.first();
                    if (event instanceof ProviderEvent.ProviderAdded) {
                        ProviderEvent.ProviderAdded messageChanged = (ProviderEvent.ProviderAdded) event;
                        eventToPublish = new ProviderEventPublish.ProviderAdded(
                                messageChanged.getProviderDTO(), messageChanged.getEventTime()
                        );
                    } else if (event instanceof ProviderEvent.ProviderUpdated) {
                        ProviderEvent.ProviderUpdated messageChanged = (ProviderEvent.ProviderUpdated) event;
                        eventToPublish = new ProviderEventPublish.ProviderUpdated(
                                messageChanged.getProviderDTO(), messageChanged.getEventTime()
                        );
                    } else if (event instanceof ProviderEvent.ProviderDeleted) {
                        ProviderEvent.ProviderDeleted messageChanged = (ProviderEvent.ProviderDeleted) event;
                        eventToPublish = new ProviderEventPublish.ProviderDeleted(
                                messageChanged.getProviderId(), messageChanged.getEventTime()
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
    private ProviderCommand.Accepted handleConfirmation(ProviderCommand.Confirmation confirmation) {
        if (confirmation instanceof ProviderCommand.Accepted) {
            return (ProviderCommand.Accepted) confirmation;
        }

        ProviderCommand.Rejected rejected = (ProviderCommand.Rejected) confirmation;
        throw new BadRequest(rejected.getReason());
    }
}
