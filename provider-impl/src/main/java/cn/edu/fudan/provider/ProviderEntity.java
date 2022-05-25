package cn.edu.fudan.provider;

import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import cn.edu.fudan.provider.domain.ProviderDTO;
import cn.edu.fudan.provider.domain.ProviderParam;
import com.lightbend.lagom.javadsl.persistence.AkkaTaggerAdapter;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * @author fuwuchen
 * @date 2022/5/19 18:16
 */
public class ProviderEntity
        extends EventSourcedBehaviorWithEnforcedReplies<ProviderCommand, ProviderEvent, ProviderState> {

    public static EntityTypeKey<ProviderCommand> ENTITY_TYPE_KEY =
            EntityTypeKey
                    .create(ProviderCommand.class, "ProviderAggregate");

    final private EntityContext<ProviderCommand> entityContext;
    final private String entityId;
    private final Function<ProviderEvent, Set<String>> tagger;

    ProviderEntity(EntityContext<ProviderCommand> entityContext) {
        super(
                PersistenceId.of(
                        entityContext.getEntityTypeKey().name(),
                        entityContext.getEntityId()
                )
        );
        this.entityContext = entityContext;
        this.entityId = entityContext.getEntityId();
        this.tagger = AkkaTaggerAdapter.fromLagom(entityContext, ProviderEvent.TAG);
    }

    @Override
    public Set<String> tagsFor(ProviderEvent providerEvent) {
        return tagger.apply(providerEvent);
    }

    @Override
    public RetentionCriteria retentionCriteria() {
        return RetentionCriteria.snapshotEvery(100, 2);
    }

    public static ProviderEntity create(EntityContext<ProviderCommand> entityContext) {
        return new ProviderEntity(entityContext);
    }

    @Override
    public ProviderState emptyState() {
        return ProviderState.EMPTY;
    }

    @Override
    public EventHandler<ProviderState, ProviderEvent> eventHandler() {

        EventHandlerBuilder<ProviderState, ProviderEvent> builder = newEventHandlerBuilder();

        builder.forAnyState()
                .onEvent(ProviderEvent.ProviderAdded.class, (providerState, event) ->
                        providerState.updateProvider(event.getProviderDTO()))
                .onEvent(ProviderEvent.ProviderUpdated.class, ((providerState, event) ->
                        providerState.updateProvider(event.getProviderDTO())))
                .onEvent(ProviderEvent.ProviderDeleted.class, ((providerState, event) ->
                        providerState.deleteProvider(event.providerId)))
        ;

        return builder.build();
    }

    @Override
    public CommandHandlerWithReply<ProviderCommand, ProviderEvent, ProviderState> commandHandler() {
        CommandHandlerWithReplyBuilder<ProviderCommand, ProviderEvent, ProviderState> builder
                = new CommandHandlerWithReplyBuilder<>();

        builder.forAnyState()
                .onCommand(ProviderCommand.Add.class, this::onAddProvider)
                .onCommand(ProviderCommand.FindById.class, this::onFindById)
                .onCommand(ProviderCommand.UpdateById.class, this::onUpdateById)
                .onCommand(ProviderCommand.DeleteById.class, this::onDeleteById)
        ;

        return builder.build();
    }


    private ReplyEffect<ProviderEvent, ProviderState> onAddProvider(ProviderState providerState,
                                                                    ProviderCommand.Add cmd) {
        if (providerState.hasProvider(cmd.getProviderParam().getId())) {
            return Effect().reply(cmd.replyTo, new ProviderCommand.Rejected("Provider already added."));
        } else {
            ProviderDTO providerDTO = new ProviderDTO(cmd.providerParam);
            return Effect()
                    .persist(new ProviderEvent.ProviderAdded(providerDTO, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new ProviderCommand.ReplyProvider(providerDTO));
        }
    }

    private ReplyEffect<ProviderEvent, ProviderState> onFindById(ProviderState providerState,
                                                                    ProviderCommand.FindById cmd) {
        if (!providerState.hasProvider(cmd.getProviderId())) {
            return Effect()
                    .reply(cmd.replyTo, new ProviderCommand.Rejected("Provider does not exist."));
        } else {
            ProviderDTO currentProvider = providerState.provider;
            return Effect()
                    .reply(cmd.replyTo, new ProviderCommand.ReplyProvider(currentProvider));
        }
    }

    private ReplyEffect<ProviderEvent, ProviderState> onUpdateById(ProviderState providerState,
                                                                 ProviderCommand.UpdateById cmd) {
        String providerId = cmd.providerParam.getId();
        if (!providerState.hasProvider(providerId)) {
            return Effect().reply(cmd.replyTo, new ProviderCommand.Rejected("Provider does not exist."));
        } else {
            ProviderDTO currentProvider = providerState.provider;
            ProviderParam toUpdate = cmd.providerParam;
            String mobile = Objects.nonNull(toUpdate.getMobile()) ? toUpdate.getMobile() : currentProvider.getMobile();
            String name = Objects.nonNull(toUpdate.getName()) ? toUpdate.getName() : currentProvider.getName();
            Long since = Objects.nonNull(toUpdate.getSince()) ? toUpdate.getSince() : currentProvider.getSince();
            ProviderDTO newProvider = new ProviderDTO(
                    providerId,
                    name,
                    mobile,
                    since,
                    currentProvider.getRating()
            );
            return Effect()
                    .persist(new ProviderEvent.ProviderUpdated(newProvider, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new ProviderCommand.ReplyProvider(newProvider));
        }
    }

    private ReplyEffect<ProviderEvent, ProviderState> onDeleteById(ProviderState providerState,
                                                                 ProviderCommand.DeleteById cmd) {
        if (!providerState.hasProvider(cmd.getProviderId())) {
            return Effect()
                    .reply(cmd.replyTo, new ProviderCommand.Rejected("Provider has been deleted."));
        } else {
            return Effect()
                    .persist(new ProviderEvent.ProviderDeleted(cmd.providerId, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new ProviderCommand.Deleted(DeleteStatus.SUCCESS));
        }
    }
}
