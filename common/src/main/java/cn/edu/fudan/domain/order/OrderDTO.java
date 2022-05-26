package cn.edu.fudan.domain.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
@Value
@JsonDeserialize
public class OrderDTO {
    public static final OrderDTO EMPTY = null;
    String id;

    String service;
    String provider;
    String consumer;
    Float cost;
    Long start;
    Long end;
    Float rating;
    String status;

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
}
