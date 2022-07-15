package cn.edu.fudan.service;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.Value;

import java.math.BigDecimal;

/**
 * @author fuwuchen
 * @date 2022/5/19 16:30
 */
@Data
@JsonDeserialize
public class ServiceParam {
    String id;
    String fields;

    /**
     * for put and post
     */
    String type;
    String providerId;
    String area;
    Integer cost;
    Integer rating;
    String status;

    public ServiceDTO toService() {
        return ServiceDTO.builder()
                .id(id)
                .type(type)
                .providerId(providerId)
                .area(area)
                .cost(cost)
                .rating(rating)
                .status(status)
                .build();
    }
}
