package cn.edu.fudan;

import akka.actor.typed.SupervisorStrategy;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import cn.edu.fudan.domain.order.OrderDTO;
import cn.edu.fudan.domain.order.OrderParam;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

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

        builder.forAnyState()
                .onEvent(OrderEvent.OrderAdded.class, (orderState, event) ->
                        orderState.updateOrder(event.getOrderDTO()))
                .onEvent(OrderEvent.OrderUpdated.class, ((orderState, event) ->
                        orderState.updateOrder(event.getOrderDTO())))
                .onEvent(OrderEvent.OrderDeleted.class, ((orderState, event) ->
                        orderState.deleteOrder(event.orderId)))
        ;

        return builder.build();
    }

    @Override
    public CommandHandlerWithReply<OrderCommand, OrderEvent, OrderState> commandHandler() {
        CommandHandlerWithReplyBuilder<OrderCommand, OrderEvent, OrderState> builder
                = new CommandHandlerWithReplyBuilder<>();

        builder.forAnyState()
                .onCommand(OrderCommand.Add.class, this::onAddOrder)
                .onCommand(OrderCommand.FindById.class, this::onFindById)
                .onCommand(OrderCommand.UpdateById.class, this::onUpdateById)
                .onCommand(OrderCommand.DeleteById.class, this::onDeleteById)
        ;

        return builder.build();
    }


    private ReplyEffect<OrderEvent, OrderState> onAddOrder(OrderState orderState,
                                                                    OrderCommand.Add cmd) {
        if (orderState.hasOrder(cmd.getOrderParam().getId())) {
            return Effect().reply(cmd.replyTo, new OrderCommand.Rejected("Order already added."));
        } else {
            OrderDTO orderDTO = new OrderDTO(cmd.orderParam);
            return Effect()
                    .persist(new OrderEvent.OrderAdded(orderDTO, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new OrderCommand.ReplyOrder(orderDTO));
        }
    }

    private ReplyEffect<OrderEvent, OrderState> onFindById(OrderState orderState,
                                                                 OrderCommand.FindById cmd) {
        if (!orderState.hasOrder(cmd.getOrderId())) {
            return Effect()
                    .reply(cmd.replyTo, new OrderCommand.Rejected("Order does not exist."));
        } else {
            OrderDTO currentOrder = orderState.order;
            return Effect()
                    .reply(cmd.replyTo, new OrderCommand.ReplyOrder(currentOrder));
        }
    }

    private ReplyEffect<OrderEvent, OrderState> onUpdateById(OrderState orderState,
                                                                   OrderCommand.UpdateById cmd) {
        String orderId = cmd.orderParam.getId();
        if (!orderState.hasOrder(orderId)) {
            return Effect().reply(cmd.replyTo, new OrderCommand.Rejected("Order does not exist."));
        } else {
            OrderDTO currentOrder = orderState.order;
            OrderParam toUpdate = cmd.orderParam;
            String service = Objects.nonNull(toUpdate.getService()) ? toUpdate.getService() : currentOrder.getService();
            String provider = Objects.nonNull(toUpdate.getProvider()) ? toUpdate.getProvider() : currentOrder.getProvider();
            String consumer = Objects.nonNull(toUpdate.getConsumer()) ? toUpdate.getConsumer() : currentOrder.getConsumer();
            Float cost = Objects.nonNull(toUpdate.getCost()) ? toUpdate.getCost() : currentOrder.getCost();
            Long start = Objects.nonNull(toUpdate.getStart()) ? toUpdate.getStart() : currentOrder.getStart();
            Long end = Objects.nonNull(toUpdate.getEnd()) ? toUpdate.getEnd() : currentOrder.getEnd();
            Float rating = Objects.nonNull(toUpdate.getRating()) ? toUpdate.getRating() : currentOrder.getRating();
            String status = Objects.nonNull(toUpdate.getStatus()) ? toUpdate.getStatus() : currentOrder.getStatus();
            OrderDTO newOrder = new OrderDTO(
                    orderId,service, provider,consumer,cost,start,end,rating,status
            );
            return Effect()
                    .persist(new OrderEvent.OrderUpdated(newOrder, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new OrderCommand.ReplyOrder(newOrder));
        }
    }

    private ReplyEffect<OrderEvent, OrderState> onDeleteById(OrderState orderState,
                                                                   OrderCommand.DeleteById cmd) {
        if (!orderState.hasOrder(cmd.getOrderId())) {
            return Effect()
                    .reply(cmd.replyTo, new OrderCommand.Rejected("Order has been deleted."));
        } else {
            return Effect()
                    .persist(new OrderEvent.OrderDeleted(cmd.orderId, Instant.now()))
                    .thenReply(cmd.replyTo, state -> new OrderCommand.Deleted(DeleteStatus.SUCCESS));
        }
    }

}
