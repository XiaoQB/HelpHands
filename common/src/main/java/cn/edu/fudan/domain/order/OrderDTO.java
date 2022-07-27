package cn.edu.fudan.domain.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.util.Objects;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
@Value
@Builder
@JsonDeserialize
public class OrderDTO {
    String id;

    String service;
    String provider;
    String consumer;
    Float cost;
    Long start;
    Long end;
    Float rating;
    String status;

    public static final OrderDTO EMPTY = OrderDTO.builder().build();

    public OrderDTO(OrderParam orderParam) {
        this.id = orderParam.getId();
        this.service = orderParam.getService();
        this.provider = orderParam.getProvider();
        this.consumer = orderParam.getConsumer();
        this.cost = orderParam.getCost();
        this.start = orderParam.getStart();
        this.end = orderParam.getEnd();
        this.rating = orderParam.getRating();
        this.status = orderParam.getStatus();
    }

    public OrderDTO(String id, String service, String provider, String consumer, Float cost, Long start, Long end, Float rating, String status) {
        this.id = id;
        this.service = service;
        this.provider = provider;
        this.consumer = consumer;
        this.cost = cost;
        this.start = start;
        this.end = end;
        this.rating = rating;
        this.status = status;
    }

    public OrderDTO mergeWithUpdate(OrderParam toUpdate) {
        return OrderDTO.builder()
                .id(Objects.nonNull(toUpdate.getId()) ? toUpdate.getId() : id)
                .service(Objects.nonNull(toUpdate.getService()) ? toUpdate.getService() : service)
                .provider(Objects.nonNull(toUpdate.getProvider()) ? toUpdate.getProvider() : provider)
                .consumer(Objects.nonNull(toUpdate.getConsumer()) ? toUpdate.getConsumer() : consumer)
                .cost(Objects.nonNull(toUpdate.getCost()) ? toUpdate.getCost() : cost)
                .start(Objects.nonNull(toUpdate.getStart()) ? toUpdate.getStart() : start)
                .end(Objects.nonNull(toUpdate.getEnd()) ? toUpdate.getEnd() : end)
                .rating(Objects.nonNull(toUpdate.getRating()) ? toUpdate.getRating() : rating)
                .status(Objects.nonNull(toUpdate.getStatus()) ? toUpdate.getStatus() : status)
                .build();
    }
}
