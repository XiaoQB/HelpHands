package cn.edu.fudan.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.time.Instant;

/**
 * @author fuwuchen
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ServiceEventPublish.ServiceAdded.class, name = "service-added"),
        @JsonSubTypes.Type(value = ServiceEventPublish.ServiceUpdated.class, name = "service-updated"),
        @JsonSubTypes.Type(value = ServiceEventPublish.ServiceDeleted.class, name = "service-deleted")
})
public interface ServiceEventPublish {
    /**
     * topic 消息分区字段
     * @return uuid of provider by default
     */
    String getPartitionKey();

    @Value
    @JsonDeserialize
    class ServiceAdded implements ServiceEventPublish {

        public ServiceDTO serviceDTO;
        public Instant eventTime;

        @JsonCreator
        public ServiceAdded(ServiceDTO serviceDTO, Instant eventTime) {
            this.serviceDTO = Preconditions.checkNotNull(serviceDTO, "serviceDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        /**
         * topic 消息分区字段
         *
         * @return uuid of provider by default
         */
        @Override
        public String getPartitionKey() {
            return serviceDTO.getId();
        }
    }

    @Value
    @JsonDeserialize
    class ServiceUpdated implements ServiceEventPublish {

        public ServiceDTO serviceDTO;
        public Instant eventTime;

        @JsonCreator
        public ServiceUpdated(ServiceDTO serviceDTO, Instant eventTime) {
            this.serviceDTO = Preconditions.checkNotNull(serviceDTO, "serviceDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        /**
         * topic 消息分区字段
         *
         * @return uuid of provider by default
         */
        @Override
        public String getPartitionKey() {
            return serviceDTO.getId();
        }
    }

    @Value
    @JsonDeserialize
    class ServiceDeleted implements ServiceEventPublish {

        public String serviceId;
        public Instant eventTime;

        @JsonCreator
        public ServiceDeleted(String serviceId, Instant eventTime) {
            this.serviceId = Preconditions.checkNotNull(serviceId, "serviceId");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        /**
         * topic 消息分区字段
         *
         * @return uuid of provider by default
         */
        @Override
        public String getPartitionKey() {
            return serviceId;
        }
    }

}
