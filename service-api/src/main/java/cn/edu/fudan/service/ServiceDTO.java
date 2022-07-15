package cn.edu.fudan.service;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * @author fuwuchen
 * @date 2022/5/19 16:30
 */
@Value
@Builder
@JsonDeserialize
public class ServiceDTO {
    String id;

    String type;
    String providerId;
    String area;
    Integer cost;
    Integer rating;
    String status;

    public static final ServiceDTO EMPTY = ServiceDTO.builder().build();

    public ServiceDTO mergeWithUpdate(ServiceDTO toUpdate) {
        return ServiceDTO.builder()
                .id(Objects.nonNull(toUpdate.getId()) ? toUpdate.getId() : id)
                .type(Objects.nonNull(toUpdate.getType()) ? toUpdate.getType() : type)
                .providerId(Objects.nonNull(toUpdate.getProviderId()) ? toUpdate.getProviderId() : providerId)
                .area(Objects.nonNull(toUpdate.getArea()) ? toUpdate.getArea() : area)
                .cost(Objects.nonNull(toUpdate.getCost()) ? toUpdate.getCost() : cost)
                .rating(Objects.nonNull(toUpdate.getRating()) ? toUpdate.getRating() : rating)
                .status(Objects.nonNull(toUpdate.getStatus()) ? toUpdate.getStatus() : status)
                .build();
    }
}
