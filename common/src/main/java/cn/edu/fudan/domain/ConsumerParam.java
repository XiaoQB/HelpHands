package cn.edu.fudan.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

/**
 * @author XiaoQuanbin
 * @date 2022/5/23
 */
@Data
@JsonDeserialize
public class ConsumerParam {

    String id;
    String fields;

    String name;
    String address;
    String mobile;
    String email;
    String geo;
}
