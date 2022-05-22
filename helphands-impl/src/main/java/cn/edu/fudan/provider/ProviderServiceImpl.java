package cn.edu.fudan.provider;

import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import cn.edu.fudan.api.ProviderService;
import cn.edu.fudan.common.DeleteResult;
import cn.edu.fudan.common.DeleteStatus;
import cn.edu.fudan.common.domain.dto.ProviderDTO;
import cn.edu.fudan.common.domain.param.ProviderParam;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import java.time.Duration;
import java.util.UUID;

/**
 * @author fuwuchen
 * @date 2022/5/19 18:11
 */
public class ProviderServiceImpl implements ProviderService {

    private final PersistentEntityRegistry persistentEntityRegistry;

    private final Duration askTimeout = Duration.ofSeconds(5);
    private ClusterSharding clusterSharding;

    @Inject
    public ProviderServiceImpl(PersistentEntityRegistry persistentEntityRegistry, ClusterSharding clusterSharding){
        this.clusterSharding = clusterSharding;
        // The persistent entity registry is only required to build an event stream for the TopicProducer
        this.persistentEntityRegistry = persistentEntityRegistry;

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
