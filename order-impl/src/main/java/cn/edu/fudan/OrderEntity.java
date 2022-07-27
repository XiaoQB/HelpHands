package cn.edu.fudan;

import akka.actor.typed.SupervisorStrategy;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import cn.edu.fudan.domain.order.OrderDTO;
import com.lightbend.lagom.javadsl.persistence.AkkaTaggerAdapter;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.function.Function;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
public class OrderEntity
        extends EventSourcedBehaviorWithEnforcedReplies<OrderCommand, OrderEvent, OrderState> {

    public static EntityTypeKey<OrderCommand> ENTITY_TYPE_KEY =
            EntityTypeKey
                    .create(OrderCommand.class, "OrderAggregate");

    final private EntityContext<OrderCommand> entityContext;
    final private String entityId;
    private final Function<OrderEvent, Set<String>> tagger;



    OrderEntity(EntityContext<OrderCommand> entityContext) {
        super(
                PersistenceId.of(
                        entityContext.getEntityTypeKey().name(),
                        entityContext.getEntityId()
                ),
                SupervisorStrategy.restartWithBackoff(Duration.ofSeconds(2), Duration.ofSeconds(30), 0.2)

        );
        this.entityContext = entityContext;
        this.entityId = entityContext.getEntityId();
        this.tagger = AkkaTaggerAdapter.fromLagom(entityContext, OrderEvent.TAG);
    }

    @Override
    public Set<String> tagsFor(OrderEvent orderEvent) {
        return tagger.apply(orderEvent);
    }

    @Override
    public RetentionCriteria retentionCriteria() {
        return RetentionCriteria.snapshotEvery(100, 2);
    }


    public static OrderEntity create(EntityContext<OrderCommand> entityContext) {
        return new OrderEntity(entityContext);
    }

    @Override
    public OrderState emptyState() {
        return OrderState.EMPTY;
    }

    @Override
    public EventHandler<OrderState, OrderEvent> eventHandler() {

        EventHandlerBuilder<OrderState, OrderEvent> builder = newEventHandlerBuilder();

        builder.forState(OrderState::hasOrder)
                        .onEvent(OrderEvent.OrderUpdated.class, ((orderState, event) ->
                                orderState.updateOrder(event.getOrderDTO())))
                        .onEvent(OrderEvent.OrderDeleted.class, (((orderState, event) ->
                                orderState.deleteOrder(event.getOrderId()))));

        builder.forState(orderState -> !orderState.hasOrder())
                .onEvent(OrderEvent.OrderAdded.class, (orderState, event) ->
                        orderState.updateOrder(event.getOrderDTO()));

        return builder.build();
    }

    @Override
    public CommandHandlerWithReply<OrderCommand, OrderEvent, OrderState> commandHandler() {
        CommandHandlerWithReplyBuilder<OrderCommand, OrderEvent, OrderState> builder
                = new CommandHandlerWithReplyBuilder<>();


        builder.forState(OrderState::hasOrder)
                        .onCommand(OrderCommand.Add.class, (state, cmd) ->
                                Effect().reply(cmd.getReplyTo(),
                                        new OrderCommand.Rejected("Order already created") {
                                        }))
                        .onCommand(OrderCommand.UpdateById.class, this::onUpdateById)
                        .onCommand(OrderCommand.FindById.class, this::onFindById)
                        .onCommand(OrderCommand.DeleteById.class, this::onDeleteById);

        builder.forState(orderState -> !orderState.hasOrder())
                .onCommand(OrderCommand.Add.class, this::onAddOrder)
                .onCommand(OrderCommand.UpdateById.class, (state, cmd) ->
                        Effect().reply(cmd.getReplyTo(), new OrderCommand.Rejected("Order doesn't exists")))
                .onCommand(OrderCommand.FindById.class, (state, cmd) ->
                        Effect().reply(cmd.getReplyTo(), new OrderCommand.Rejected("Order doesn't exists")))
                .onCommand(OrderCommand.DeleteById.class, (state, cmd) ->
                        Effect().reply(cmd.getReplyTo(), new OrderCommand.Rejected("Order doesn't exists")));

        return builder.build();
    }


    private ReplyEffect<OrderEvent, OrderState> onAddOrder(OrderState orderState,
                                                                    OrderCommand.Add cmd) {
        return Effect()
                    .persist(new OrderEvent.OrderAdded(cmd.getOrderParam().toOrder(), Instant.now()))
                    .thenReply(cmd.replyTo, state -> new OrderCommand.ReplyOrder(cmd.getOrderParam().toOrder()));
    }

    private ReplyEffect<OrderEvent, OrderState> onFindById(OrderState orderState,
                                                                 OrderCommand.FindById cmd) {
        return Effect()
                .reply(cmd.replyTo, new OrderCommand.ReplyOrder(orderState.getOrder()));
    }

    private ReplyEffect<OrderEvent, OrderState> onUpdateById(OrderState orderState,
                                                                   OrderCommand.UpdateById cmd) {
        OrderDTO updatedOrderDTO = orderState.getOrder().mergeWithUpdate(cmd.orderParam);
        return Effect()
                    .persist(new OrderEvent.OrderUpdated(updatedOrderDTO, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new OrderCommand.ReplyOrder(updatedOrderDTO));
    }

    private ReplyEffect<OrderEvent, OrderState> onDeleteById(OrderState orderState,
                                                                   OrderCommand.DeleteById cmd) {
        return Effect()
                .persist(new OrderEvent.OrderDeleted(cmd.getOrderId(), Instant.now()))
                .thenReply(cmd.replyTo, state -> new OrderCommand.Deleted(DeleteStatus.SUCCESS));

    }
}
