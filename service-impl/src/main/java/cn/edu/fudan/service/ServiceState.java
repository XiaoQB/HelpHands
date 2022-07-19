package cn.edu.fudan.service;

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
public class ServiceState implements CompressedJsonable {
    ServiceDTO service;

    @JsonCreator
    ServiceState(ServiceDTO serviceDTO) {
        this.service = Preconditions.checkNotNull(serviceDTO, "service");
    }

    ServiceState deleteService(String serviceId) {
        if (Objects.equals(serviceId, service.getId())) {
            return new ServiceState(ServiceDTO.EMPTY);
        }
        return this;
    }

    ServiceState updateService(ServiceDTO serviceDTO) {
        return new ServiceState(serviceDTO);
    }

    boolean hasService() {
        return service.getId() != null;
    }

    boolean nonService() {
        return !hasService();
    }

    public static final ServiceState EMPTY = new ServiceState(ServiceDTO.EMPTY);

    @Override
    public String toString() {
        return "ServiceState{" +
                "services=" + service +
                '}';
    }
}
