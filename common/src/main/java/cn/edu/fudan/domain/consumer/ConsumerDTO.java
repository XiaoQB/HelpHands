package cn.edu.fudan.domain.consumer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

/**
 * @author XiaoQuanbin
 * @date 2022/5/23
 */
@Value
@JsonDeserialize
public class ConsumerDTO {

    String id;
    String name;
    String address;
    String mobile;
    String email;
    String geo;

    public ConsumerDTO(String id, String name, String address, String mobile, String email, String geo) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.mobile = mobile;
        this.email = email;
        this.geo = geo;
    }

    public ConsumerDTO(ConsumerParam consumerParam) {
        this.id = consumerParam.getId();
        this.name = consumerParam.getName();
        this.address = consumerParam.getAddress();
        this.mobile = consumerParam.getMobile();
        this.email = consumerParam.getEmail();
        this.geo = consumerParam.getGeo();
    }

    public static final ConsumerDTO EMPTY = new ConsumerDTO("","","","","","");
}
