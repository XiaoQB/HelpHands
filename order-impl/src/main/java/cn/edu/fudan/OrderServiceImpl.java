package cn.edu.fudan;

import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.cluster.sharding.typed.javadsl.EntityRef;
import akka.japi.Pair;
import cn.edu.fudan.domain.consumer.ConsumerParam;
import cn.edu.fudan.domain.order.OrderDTO;
import cn.edu.fudan.domain.order.OrderParam;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;
import java.time.Duration;
import java.util.UUID;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
public class OrderServiceImpl implements OrderService{

    private final Duration askTimeout = Duration.ofSeconds(5);

    private final PersistentEntityRegistry persistentEntityRegistry;
    private final ClusterSharding clusterSharding;

    @Inject
    public OrderServiceImpl(PersistentEntityRegistry persistentEntityRegistry,
                            ClusterSharding clusterSharding) {

        this.persistentEntityRegistry = persistentEntityRegistry;
        this.clusterSharding = clusterSharding;
        this.clusterSharding.init(
                Entity.of(
                        OrderEntity.ENTITY_TYPE_KEY,
                        OrderEntity::create
                )
        );
    }

    /**
     * Gets all the orders placed by the authenticated consumer.
     *
     * @return orders
     */
    @Override
    public ServiceCall<ConsumerParam, OrderDTO> findOfConsumer() {
        return null;
    }

    /**
     * Gets the details of the order with the specified :id and placed by the authenticated consumer.
     *
     * @param id
     * @return order
     */
    @Override
    public ServiceCall<NotUsed, OrderDTO> get(String id) {
        return request -> {
            EntityRef<OrderCommand> ref = entityRefFor(id);
            return ref.<OrderCommand.Confirmation>ask(replyTo -> new OrderCommand.FindById(id, replyTo), askTimeout)
                    .thenApplyAsync(this::handleConfirmation)
                    .thenApplyAsync(accpted -> (OrderDTO) accpted.get());
        };
    }

    /**
     * Creates a new order with the specified ID for the authenticated consumer.
     *
     * @param id order id
     * @return updated OrderDTO
     */
    @Override
    public ServiceCall<OrderParam, OrderDTO> modify(String id) {
        return null;
    }

    /**
     * Creates a new order for the authenticated consumer and returns the ID.
     *
     * @return the order id
     */
    @Override
    public ServiceCall<OrderParam, OrderDTO> add() {
        return request -> {
            request.setId(UUID.randomUUID().toString());
            
            EntityRef<OrderCommand> ref = entityRefFor(request.getId());
            return ref.<OrderCommand.Confirmation>ask(replyTo -> new OrderCommand.Add(request, replyTo), askTimeout)
                    .thenApplyAsync(this::handleConfirmation)
                    .thenApplyAsync(accepted -> (OrderDTO) accepted.get());
        };
    }

    private EntityRef<OrderCommand> entityRefFor(String id) {
        return clusterSharding.entityRefFor(OrderEntity.ENTITY_TYPE_KEY, id);
    }

    /**
     * Adds to the latest ratings for the order.
     *
     * @return updated OrderDTO
     */
    @Override
    public ServiceCall<OrderParam, OrderDTO> rate() {
        return null;
    }

    /**
     * Deletes the order with the specified ID for the authenticated consumer.
     *
     * @param id
     * @return delete status enum
     */
    @Override
    public ServiceCall<NotUsed, DeleteResult<String>> delete(String id) {
        return request -> {
            // Look up the aggregate instance for the given ID.
            EntityRef<OrderCommand> ref = entityRefFor(id);
            return ref.
                    <OrderCommand.Confirmation>ask(
                    replyTo -> new OrderCommand.DeleteById(id, replyTo), askTimeout)
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
    public Topic<OrderEventPublish> orderEvent() {
        return TopicProducer.taggedStreamWithOffset(OrderEvent.TAG.allTags(), (tag, offset) ->
                // Load the event stream for the passed in shard tag
                persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {
                    OrderEventPublish eventToPublish;
                    OrderEvent event = eventAndOffset.first();
                    if (event instanceof OrderEvent.OrderAdded) {
                        OrderEvent.OrderAdded messageChanged = (OrderEvent.OrderAdded) event;
                        eventToPublish = new OrderEventPublish.OrderAdded(
                                messageChanged.getOrderDTO(), messageChanged.getEventTime()
                        );
                    } else if (event instanceof OrderEvent.OrderUpdated) {
                        OrderEvent.OrderUpdated messageChanged = (OrderEvent.OrderUpdated) event;
                        eventToPublish = new OrderEventPublish.OrderUpdated(
                                messageChanged.getOrderDTO(), messageChanged.getEventTime()
                        );
                    } else if (event instanceof OrderEvent.OrderDeleted) {
                        OrderEvent.OrderDeleted messageChanged = (OrderEvent.OrderDeleted) event;
                        eventToPublish = new OrderEventPublish.OrderDeleted(
                                messageChanged.getOrderId(), messageChanged.getEventTime()
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
    private OrderCommand.Accepted handleConfirmation(OrderCommand.Confirmation confirmation) {
        if (confirmation instanceof OrderCommand.Accepted) {
            return (OrderCommand.Accepted) confirmation;
        }

        OrderCommand.Rejected rejected = (OrderCommand.Rejected) confirmation;
        throw new BadRequest(rejected.getReason());
    }
}
