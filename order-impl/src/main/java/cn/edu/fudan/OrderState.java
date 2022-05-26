package cn.edu.fudan;

import cn.edu.fudan.domain.order.OrderDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
public class OrderState {
    public final OrderDTO order;

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

    boolean hasOrder(String orderId) {
        return Objects.equals(order.getId(), orderId);
    }

    public static final OrderState EMPTY = new OrderState(OrderDTO.EMPTY);

    @Override
    public String toString() {
        return "OrderState{" +
                "orders=" + order +
                '}';
    }
}
