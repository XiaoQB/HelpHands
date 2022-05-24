package cn.edu.fudan;

import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import cn.edu.fudan.domain.ConsumerDTO;
import cn.edu.fudan.domain.ConsumerParam;

import java.time.Instant;
import java.util.Objects;

/**
 * @author XiaoQuanbin
 * @date 2022/5/23
 */
public class ConsumerEntity
        extends EventSourcedBehaviorWithEnforcedReplies<ConsumerCommand, ConsumerEvent, ConsumerState> {

    public static EntityTypeKey<ConsumerCommand> ENTITY_TYPE_KEY =
            EntityTypeKey
                    .create(ConsumerCommand.class, "ConsumerAggregate");

    final private EntityContext<ConsumerCommand> entityContext;
    final private String entityId;


    ConsumerEntity(EntityContext<ConsumerCommand> entityContext) {
        super(
                PersistenceId.of(
                        entityContext.getEntityTypeKey().name(),
                        entityContext.getEntityId()
                )
        );
        this.entityContext = entityContext;
        this.entityId = entityContext.getEntityId();
    }

    public static ConsumerEntity create(EntityContext<ConsumerCommand> entityContext) {
        return new ConsumerEntity(entityContext);
    }

    @Override
    public ConsumerState emptyState() {
        return ConsumerState.EMPTY;
    }

    @Override
    public EventHandler<ConsumerState, ConsumerEvent> eventHandler() {

        EventHandlerBuilder<ConsumerState, ConsumerEvent> builder = newEventHandlerBuilder();

        builder.forAnyState()
                .onEvent(ConsumerEvent.ConsumerAdded.class, (consumerState, event) ->
                        consumerState.updateConsumer(event.getConsumerDTO()))
                .onEvent(ConsumerEvent.ConsumerUpdated.class, ((consumerState, event) ->
                        consumerState.updateConsumer(event.getConsumerDTO())))
                .onEvent(ConsumerEvent.ConsumerDeleted.class, ((consumerState, event) ->
                        consumerState.deleteConsumer(event.consumerId)))
        ;

        return builder.build();
    }

    @Override
    public CommandHandlerWithReply<ConsumerCommand, ConsumerEvent, ConsumerState> commandHandler() {
        CommandHandlerWithReplyBuilder<ConsumerCommand, ConsumerEvent, ConsumerState> builder
                = new CommandHandlerWithReplyBuilder<>();

        builder.forAnyState()
                .onCommand(ConsumerCommand.Add.class, this::onAddConsumer)
                .onCommand(ConsumerCommand.FindById.class, this::onFindById)
                .onCommand(ConsumerCommand.UpdateById.class, this::onUpdateById)
                .onCommand(ConsumerCommand.DeleteById.class, this::onDeleteById)
        ;

        return builder.build();
    }


    private ReplyEffect<ConsumerEvent, ConsumerState> onAddConsumer(ConsumerState consumerState,
                                                                    ConsumerCommand.Add cmd) {
        if (consumerState.hasConsumer(cmd.getConsumerParam().getId())) {
            return Effect().reply(cmd.replyTo, new ConsumerCommand.Rejected("Consumer already added."));
        } else {
            ConsumerDTO consumerDTO = new ConsumerDTO(cmd.consumerParam);
            return Effect()
                    .persist(new ConsumerEvent.ConsumerAdded(consumerDTO, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new ConsumerCommand.ReplyConsumer(consumerDTO));
        }
    }

    private ReplyEffect<ConsumerEvent, ConsumerState> onFindById(ConsumerState consumerState,
                                                                 ConsumerCommand.FindById cmd) {
        if (!consumerState.hasConsumer(cmd.getConsumerId())) {
            return Effect()
                    .reply(cmd.replyTo, new ConsumerCommand.Rejected("Consumer does not exist."));
        } else {
            ConsumerDTO currentConsumer = consumerState.consumer;
            return Effect()
                    .reply(cmd.replyTo, new ConsumerCommand.ReplyConsumer(currentConsumer));
        }
    }

    private ReplyEffect<ConsumerEvent, ConsumerState> onUpdateById(ConsumerState consumerState,
                                                                   ConsumerCommand.UpdateById cmd) {
        String consumerId = cmd.consumerParam.getId();
        if (!consumerState.hasConsumer(consumerId)) {
            return Effect().reply(cmd.replyTo, new ConsumerCommand.Rejected("Consumer does not exist."));
        } else {
            ConsumerDTO currentConsumer = consumerState.consumer;
            ConsumerParam toUpdate = cmd.consumerParam;
            String name = Objects.nonNull(toUpdate.getName()) ? toUpdate.getName() : currentConsumer.getName();
            String address = Objects.nonNull(toUpdate.getAddress()) ? toUpdate.getAddress() : currentConsumer.getAddress();
            String mobile = Objects.nonNull(toUpdate.getMobile()) ? toUpdate.getMobile() : currentConsumer.getMobile();
            String email = Objects.nonNull(toUpdate.getEmail()) ? toUpdate.getEmail() : currentConsumer.getEmail();
            String geo = Objects.nonNull(toUpdate.getGeo()) ? toUpdate.getGeo() : currentConsumer.getGeo();
            ConsumerDTO newConsumer = new ConsumerDTO(
                    consumerId,
                    name,
                    address,
                    mobile,
                    email,
                    geo
            );
            return Effect()
                    .persist(new ConsumerEvent.ConsumerUpdated(newConsumer, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new ConsumerCommand.ReplyConsumer(newConsumer));
        }
    }

    private ReplyEffect<ConsumerEvent, ConsumerState> onDeleteById(ConsumerState consumerState,
                                                                   ConsumerCommand.DeleteById cmd) {
        if (!consumerState.hasConsumer(cmd.getConsumerId())) {
            return Effect()
                    .reply(cmd.replyTo, new ConsumerCommand.Rejected("Consumer has been deleted."));
        } else {
            return Effect()
                    .persist(new ConsumerEvent.ConsumerDeleted(cmd.consumerId, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new ConsumerCommand.Deleted(DeleteStatus.SUCCESS));
        }
    }

}
