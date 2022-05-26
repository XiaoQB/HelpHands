package cn.edu.fudan;

import cn.edu.fudan.domain.order.OrderDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.time.Instant;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
public interface OrderEvent extends Jsonable, AggregateEvent<OrderEvent> {
    /**
     * Tags are used for getting and publishing streams of events. Each event
     * will have this tag, and in this case, we are partitioning the tags into
     * 4 shards, which means we can have 4 concurrent processors/publishers of
     * events.
     */
    AggregateEventShards<OrderEvent> TAG = AggregateEventTag.sharded(OrderEvent.class, 4);

    @Override
    default AggregateEventTagger<OrderEvent> aggregateTag() {
        return TAG;
    }

    @Value
    @JsonDeserialize
    class OrderAdded implements OrderEvent{

        public OrderDTO orderDTO;
        public Instant eventTime;

        @JsonCreator
        public OrderAdded(OrderDTO orderDTO, Instant eventTime) {
            this.orderDTO = Preconditions.checkNotNull(orderDTO, "orderDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Value
    @JsonDeserialize
    class OrderUpdated implements OrderEvent{

        public OrderDTO orderDTO;
        public Instant eventTime;

        @JsonCreator
        public OrderUpdated(OrderDTO orderDTO, Instant eventTime) {
            this.orderDTO = Preconditions.checkNotNull(orderDTO, "orderDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Value
    @JsonDeserialize
    class OrderDeleted implements OrderEvent{

        public String orderId;
        public Instant eventTime;

        @JsonCreator
        public OrderDeleted(String orderId, Instant eventTime) {
            this.orderId = Preconditions.checkNotNull(orderId, "orderId");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }
}
