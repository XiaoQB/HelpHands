package cn.edu.fudan;

import cn.edu.fudan.domain.order.OrderDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.util.Objects;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
@Value
@JsonDeserialize
public class OrderState {
    OrderDTO order;

    @JsonCreator
    OrderState(OrderDTO order) {
        this.order = Preconditions.checkNotNull(order, "order");
    }

    OrderState deleteOrder(String orderId) {
        if (Objects.equals(orderId, order.getId())) {
            return new OrderState(OrderDTO.EMPTY);
        }
        return this;
    }

    OrderState updateOrder(OrderDTO orderDTO) {
        return new OrderState(orderDTO);
    }

    boolean hasOrder() {
        return order.getId() != null;
    }

    public static final OrderState EMPTY = new OrderState(OrderDTO.EMPTY);

    @Override
    public String toString() {
        return "OrderState{" +
                "orders=" + order +
                '}';
    }
}
