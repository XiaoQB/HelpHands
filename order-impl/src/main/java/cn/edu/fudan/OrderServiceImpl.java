package cn.edu.fudan;

import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import akka.persistence.cassandra.session.javadsl.CassandraSession;
import cn.edu.fudan.domain.consumer.ConsumerParam;
import cn.edu.fudan.domain.order.OrderDTO;
import cn.edu.fudan.domain.order.OrderParam;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;

import javax.inject.Inject;
import java.time.Duration;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
public class OrderServiceImpl implements OrderService{

    private final Duration askTimeout = Duration.ofSeconds(5);

    private final CassandraSession cassandraSession;
    private final PersistentEntityRegistry persistentEntityRegistry;
    private final ClusterSharding clusterSharding;

    @Inject
    public OrderServiceImpl(CassandraSession cassandraSession,
                            PersistentEntityRegistry persistentEntityRegistry,
                            ClusterSharding clusterSharding,
                            ReadSide readSide) {
        readSide.register(OrderEventProcessor.class);
        this.cassandraSession = cassandraSession;
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
        return null;
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
        return null;
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
        return null;
    }
}
