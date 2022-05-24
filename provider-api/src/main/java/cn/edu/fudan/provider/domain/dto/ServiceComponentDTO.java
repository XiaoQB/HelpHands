package cn.edu.fudan.provider.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

/**
 * @author fuwuchen
 * @date 2022/5/19 16:30
 */
@Value
@JsonDeserialize
public class ServiceComponentDTO {
    Id id;

    String type;
    String provider;
    String area;
    Float cost;
    Float rating;
    String status;

    @Value
    @JsonDeserialize
    public static class Id {
        String id;
    }
}
