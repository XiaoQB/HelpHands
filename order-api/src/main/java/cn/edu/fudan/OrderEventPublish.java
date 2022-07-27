package cn.edu.fudan;

import cn.edu.fudan.domain.order.OrderDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.time.Instant;

/**
 * @author XiaoQuanbin
 * @date 2022/7/26
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderEventPublish.OrderAdded.class, name = "order-added"),
        @JsonSubTypes.Type(value = OrderEventPublish.OrderUpdated.class, name = "order-updated"),
        @JsonSubTypes.Type(value = OrderEventPublish.OrderDeleted.class, name = "order-deleted")
})
public interface OrderEventPublish {
    /**
     * topic 消息分区字段
     * @return uuid of provider by default
     */
    String getPartitionKey();

    @Value
    @JsonDeserialize
    class OrderAdded implements OrderEventPublish {

        public OrderDTO orderDTO;
        public Instant eventTime;

        @JsonCreator
        public OrderAdded(OrderDTO orderDTO, Instant eventTime) {
            this.orderDTO = Preconditions.checkNotNull(orderDTO, "orderDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        /**
         * topic 消息分区字段
         *
         * @return uuid of provider by default
         */
        @Override
        public String getPartitionKey() {
            return orderDTO.getId();
        }
    }

    @Value
    @JsonDeserialize
    class OrderUpdated implements OrderEventPublish {

        public OrderDTO orderDTO;
        public Instant eventTime;

        @JsonCreator
        public OrderUpdated(OrderDTO orderDTO, Instant eventTime) {
            this.orderDTO = Preconditions.checkNotNull(orderDTO, "orderDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        /**
         * topic 消息分区字段
         *
         * @return uuid of provider by default
         */
        @Override
        public String getPartitionKey() {
            return orderDTO.getId();
        }
    }

    @Value
    @JsonDeserialize
    class OrderDeleted implements OrderEventPublish {

        public String orderId;
        public Instant eventTime;

        @JsonCreator
        public OrderDeleted(String orderId, Instant eventTime) {
            this.orderId = Preconditions.checkNotNull(orderId, "orderId");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        /**
         * topic 消息分区字段
         *
         * @return uuid of provider by default
         */
        @Override
        public String getPartitionKey() {
            return orderId;
        }
    }

}
