package cn.edu.fudan.provider;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Value;

/**
 * @author fuwuchen
 * @date 2022/5/19 16:30
 */
@Value
@JsonDeserialize
public class ProviderDTO {

    String id;
    String name;
    String mobile;
    Long since;
    Float rating;

    public ProviderDTO(String id, String name, String mobile, Long since, Float rating) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.since = since;
        this.rating = rating;
    }

    public ProviderDTO(ProviderParam providerParam) {
        this.id = providerParam.getId();
        this.name = providerParam.getName();
        this.mobile = providerParam.getMobile();
        this.since = providerParam.getSince();
        this.rating = providerParam.getRating();
    }

    public static final ProviderDTO EMPTY = new ProviderDTO("", "", "", 0L, 0.0f);

    @Override
    public String toString() {
        return "ProviderDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", since=" + since +
                ", rating=" + rating +
                '}';
    }
}
