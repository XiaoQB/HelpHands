package cn.edu.fudan.provider.domain.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

/**
 * @author fuwuchen
 * @date 2022/5/19 16:30
 */
@Value
@JsonDeserialize
public class ServiceComponentParam {
    Id id;
    String fields;

    /**
     * for put and post
     */
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
