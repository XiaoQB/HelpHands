package cn.edu.fudan.common.domain.param;

import cn.edu.fudan.common.domain.dto.ProviderDTO;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.Value;

import java.util.UUID;

/**
 * @author fuwuchen
 * @date 2022/5/19 16:30
 */
@Data
@JsonDeserialize
public class ProviderParam {

    String id;
    String fields;
    /**
     * for put and post
     */
    String name;
    String mobile;
    Long since;
    Float rating;
}
