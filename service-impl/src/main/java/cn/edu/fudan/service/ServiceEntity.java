package cn.edu.fudan.service;

import akka.actor.typed.SupervisorStrategy;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import cn.edu.fudan.DeleteStatus;
import com.lightbend.lagom.javadsl.persistence.AkkaTaggerAdapter;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.function.Function;

/**
 * @author fuwuchen
 * @date 2022/5/19 18:16
 */
public class ServiceEntity
        extends EventSourcedBehaviorWithEnforcedReplies<ServiceCommand, ServiceEvent, ServiceState> {

    public static EntityTypeKey<ServiceCommand> ENTITY_TYPE_KEY =
            EntityTypeKey
                    .create(ServiceCommand.class, "ServiceAggregate");

    final private EntityContext<ServiceCommand> entityContext;
    final private String entityId;
    private final Function<ServiceEvent, Set<String>> tagger;

    ServiceEntity(EntityContext<ServiceCommand> entityContext) {
        super(
                PersistenceId.of(
                        entityContext.getEntityTypeKey().name(),
                        entityContext.getEntityId()
                ),
                SupervisorStrategy.restartWithBackoff(
                        Duration.ofSeconds(2), Duration.ofSeconds(30), 0.2)
        );
        this.entityContext = entityContext;
        this.entityId = entityContext.getEntityId();
        this.tagger = AkkaTaggerAdapter.fromLagom(entityContext, ServiceEvent.TAG);
    }

    @Override
    public Set<String> tagsFor(ServiceEvent serviceEvent) {
        return tagger.apply(serviceEvent);
    }

    @Override
    public RetentionCriteria retentionCriteria() {
        return RetentionCriteria.snapshotEvery(100, 2);
    }

    public static ServiceEntity create(EntityContext<ServiceCommand> entityContext) {
        return new ServiceEntity(entityContext);
    }

    @Override
    public ServiceState emptyState() {
        return ServiceState.EMPTY;
    }

    @Override
    public EventHandler<ServiceState, ServiceEvent> eventHandler() {

        EventHandlerBuilder<ServiceState, ServiceEvent> builder = newEventHandlerBuilder();

        builder.forState(ServiceState::hasService)
                .onEvent(ServiceEvent.ServiceUpdated.class, ((serviceState, event) ->
                        serviceState.updateService(event.getServiceDTO())))
                .onEvent(ServiceEvent.ServiceDeleted.class, ((serviceState, event) ->
                        serviceState.deleteService(event.getServiceId())))
        ;

        builder.forState(state -> !state.hasService())
                .onEvent(ServiceEvent.ServiceAdded.class, (serviceState, event) ->
                        serviceState.updateService(event.getServiceDTO()))
        ;

        return builder.build();
    }

    @Override
    public CommandHandlerWithReply<ServiceCommand, ServiceEvent, ServiceState> commandHandler() {
        CommandHandlerWithReplyBuilder<ServiceCommand, ServiceEvent, ServiceState> builder
                = new CommandHandlerWithReplyBuilder<>();

        builder.forState(ServiceState::hasService)
                .onCommand(ServiceCommand.Add.class, (state, cmd) ->
                        Effect().reply(cmd.getReplyTo(),
                                new ServiceCommand.Rejected("Service already exists")))
                .onCommand(ServiceCommand.UpdateById.class, this::onUpdateById)
                .onCommand(ServiceCommand.DeleteById.class, this::onDeleteById)
                .onCommand(ServiceCommand.FindById.class, this::onFindById)
        ;
        builder.forState(ServiceState::nonService)
                .onCommand(ServiceCommand.Add.class, this::onAddService)
                .onCommand(ServiceCommand.UpdateById.class, (state, cmd) ->
                        Effect().reply(cmd.getReplyTo(),
                                new ServiceCommand.Rejected("Service doesn't exists")))
                .onCommand(ServiceCommand.DeleteById.class, (state, cmd) ->
                        Effect().reply(cmd.getReplyTo(),
                                new ServiceCommand.Rejected("Service doesn't exists")))
                .onCommand(ServiceCommand.FindById.class, (state, cmd) ->
                        Effect().reply(cmd.getReplyTo(),
                                new ServiceCommand.Rejected("Service doesn't exists")))
        ;
        return builder.build();
    }


    private ReplyEffect<ServiceEvent, ServiceState> onAddService(ServiceState serviceState,
                                                                 ServiceCommand.Add cmd) {
        ServiceDTO serviceDTO = cmd.getServiceParam().toService();
        return Effect()
                .persist(new ServiceEvent.ServiceAdded(serviceDTO, Instant.now()))
                .thenReply(cmd.replyTo,
                        state -> new ServiceCommand.ReplyService(state.getService()));
    }

    private ReplyEffect<ServiceEvent, ServiceState> onFindById(ServiceState serviceState,
                                                               ServiceCommand.FindById cmd) {
        ServiceDTO currentService = serviceState.getService();
        return Effect()
                .reply(cmd.replyTo, new ServiceCommand.ReplyService(currentService));
    }

    private ReplyEffect<ServiceEvent, ServiceState> onUpdateById(ServiceState serviceState,
                                                                 ServiceCommand.UpdateById cmd) {
        ServiceDTO currentService = serviceState.getService();
        ServiceParam toUpdate = cmd.serviceParam;
        ServiceDTO newService = currentService.mergeWithUpdate(toUpdate.toService());
        return Effect()
                .persist(new ServiceEvent.ServiceUpdated(newService, Instant.now()))
                .thenReply(cmd.replyTo, state -> new ServiceCommand.ReplyService(newService));
    }

    private ReplyEffect<ServiceEvent, ServiceState> onDeleteById(ServiceState serviceState,
                                                                 ServiceCommand.DeleteById cmd) {
        return Effect()
                .persist(new ServiceEvent.ServiceDeleted(cmd.getServiceId(), Instant.now()))
                .thenReply(cmd.replyTo, state -> new ServiceCommand.Deleted(DeleteStatus.SUCCESS));
    }
}
