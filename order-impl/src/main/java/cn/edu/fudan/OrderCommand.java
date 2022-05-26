package cn.edu.fudan;

import akka.actor.typed.ActorRef;
import cn.edu.fudan.domain.order.OrderDTO;
import cn.edu.fudan.domain.order.OrderParam;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
public interface OrderCommand extends Jsonable {

    @Value
    @JsonDeserialize
     class Add implements OrderCommand {
        public  OrderParam orderParam;
        public  ActorRef<Confirmation> replyTo;

        @JsonCreator
        Add(OrderParam orderParam, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.orderParam = Preconditions.checkNotNull(orderParam, "order param cannot be null");
            this.replyTo = replyTo;
        }
    }


    /**
     * Reply type for a create service order command.
     */
    @Value
    @JsonDeserialize
    class ReplyOrder implements Accepted<OrderDTO> {
        public  OrderDTO orderDto;

        @JsonCreator
        public ReplyOrder(OrderDTO orderDto) {
            this.orderDto = orderDto;
        }

        @Override
        public OrderDTO get() {
            return orderDto;
        }
    }

    @Value
    @JsonDeserialize
     class FindById implements OrderCommand {
        public  String orderId;
        public  ActorRef<Confirmation> replyTo;

        @JsonCreator
        FindById(String orderId, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.orderId = Preconditions.checkNotNull(orderId, "order param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
     class UpdateById implements OrderCommand {
        public  OrderParam orderParam;
        public  ActorRef<Confirmation> replyTo;

        @JsonCreator
        UpdateById(OrderParam orderParam, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.orderParam = Preconditions.checkNotNull(orderParam, "order param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
     class DeleteById implements OrderCommand {
        public  String orderId;
        public  ActorRef<Confirmation> replyTo;

        @JsonCreator
        DeleteById(String orderId, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.orderId = Preconditions.checkNotNull(orderId, "order param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
     class Deleted implements Accepted<DeleteStatus> {

        public DeleteStatus status;

        @JsonCreator
        public Deleted(DeleteStatus status) {
            this.status = status;
        }

        @Override
        public DeleteStatus get() {
            return status;
        }
    }




    /**
     * Super interface for Accepted/Rejected replies used by Add
     */
    interface Confirmation {
    }


    interface Accepted<T> extends Confirmation {
        /**
         * 获取正常流程的返回值
         * @return 由 Command 决定 Reply 类型
         */
        T get();
    }

    @Value
    @JsonDeserialize
    class Rejected implements Confirmation {
        public  String reason;

        @JsonCreator
        public Rejected(String reason) {
            this.reason = reason;
        }
    }
}
