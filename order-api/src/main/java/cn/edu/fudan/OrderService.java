package cn.edu.fudan;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import cn.edu.fudan.domain.order.OrderDTO;
import cn.edu.fudan.domain.order.OrderParam;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.transport.Method;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.Service.topic;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
public interface OrderService extends Service{

    /**
     * route definition
     * @return descriptor
     */
    @Override
    default Descriptor descriptor() {
        return Service.named("order").withCalls(
                restCall(Method.GET, "/orders", this::getAll),
                restCall(Method.GET, "/orders/:id", this::get),
                restCall(Method.PUT, "/orders/:id", this::modify),
                restCall(Method.POST, "/orders", this::add),
                restCall(Method.PUT, "/orders/:id/rate", this::rate),
                restCall(Method.DELETE, "/orders/:id", this::delete)
        ).withTopics(
                topic("order-events", this::orderEvent)
                        .withProperty(KafkaProperties.partitionKeyStrategy(),
                                OrderEventPublish::getPartitionKey))
                .withAutoAcl(true);
    }

    /**
     * Gets all the orders placed by the authenticated consumer.
     * @return orders
     */
    ServiceCall<NotUsed, Source<OrderDTO, ?>> getAll();

    /**
     * Gets the details of the order with the specified :id and placed by the authenticated consumer.
     * @return order
     */
    ServiceCall<NotUsed, OrderDTO> get(String id);

    /**
     * Creates a new order with the specified ID for the authenticated consumer.
      * @param id order id
     * @return updated OrderDTO
     */
    ServiceCall<OrderParam, OrderDTO> modify(String id);

    /**
     * Creates a new order for the authenticated consumer and returns the ID.
     * @return the order id
     */
    ServiceCall<OrderParam, OrderDTO> add();

    /**
     * Adds to the latest ratings for the order.
     * @return updated OrderDTO
     */
    ServiceCall<OrderParam, OrderDTO> rate();

    /**
     * Deletes the order with the specified ID for the authenticated consumer.
     * @return delete status enum
     */
    ServiceCall<NotUsed, DeleteResult<String>> delete(String id);

    /**
     * public order events to kafka
     * @return order topic
     */
    Topic<OrderEventPublish> orderEvent();

    }
