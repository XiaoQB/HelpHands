package cn.edu.fudan.domain.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

/**
 * @author XiaoQuanbin
 * @date 2022/5/24
 */
@Data
@JsonDeserialize
public class OrderParam {

    String id;

    String service;
    String provider;
    String consumer;
    Float cost;
    Long start;
    Long end;
    Float rating;
    String status;

    public OrderDTO toOrder() {
            return OrderDTO.builder()
                    .id(id)
                    .service(service)
                    .provider(provider)
                    .cost(cost)
                    .start(start)
                    .end(end)
                    .rating(rating)
                    .status(status)
                    .build();
        }
}
