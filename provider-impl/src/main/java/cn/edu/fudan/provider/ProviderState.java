package cn.edu.fudan.provider;

import cn.edu.fudan.provider.api.ProviderDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.util.Objects;

/**
 * @author fuwuchen
 * @date 2022/5/19 18:19
 */
@Value
@JsonDeserialize
public class ProviderState implements CompressedJsonable {
    public ProviderDTO provider;

    @JsonCreator
    ProviderState(ProviderDTO provider) {
        this.provider = Preconditions.checkNotNull(provider, "provider");
    }

    ProviderState deleteProvider(String providerId) {
        if (Objects.equals(providerId, provider.getId())) {
            return new ProviderState(ProviderDTO.EMPTY);
        }
        return this;
    }

    ProviderState updateProvider(ProviderDTO providerDTO) {
        return new ProviderState(providerDTO);
    }

    boolean hasProvider(String providerId) {
        return Objects.equals(provider.getId(), providerId);
    }

    public static final ProviderState EMPTY = new ProviderState(ProviderDTO.EMPTY);

    @Override
    public String toString() {
        return "ProviderState{" +
                "providers=" + provider +
                '}';
    }
}
