package cn.edu.fudan.provider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.time.Instant;

/**
 * @author fuwuchen
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ProviderEventPublish.ProviderAdded.class, name = "provider-added"),
        @JsonSubTypes.Type(value = ProviderEventPublish.ProviderUpdated.class, name = "provider-updated"),
        @JsonSubTypes.Type(value = ProviderEventPublish.ProviderDeleted.class, name = "provider-deleted")
})
public interface ProviderEventPublish {
    /**
     * topic 消息分区字段
     * @return uuid of provider by default
     */
    String getPartitionKey();

    /**
     * An event that represents a change in greeting message.
     */
    @Value
    class ProviderAdded implements ProviderEventPublish {

        public ProviderDTO providerDTO;
        public Instant eventTime;

        @JsonCreator
        public ProviderAdded(ProviderDTO providerDTO, Instant eventTime) {
            this.providerDTO = Preconditions.checkNotNull(providerDTO, "providerDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        @Override
        public String getPartitionKey() {
            return providerDTO.getId();
        }
    }

    @Value
    class ProviderUpdated implements ProviderEventPublish {

        public ProviderDTO providerDTO;
        public Instant eventTime;

        @JsonCreator
        public ProviderUpdated(ProviderDTO providerDTO, Instant eventTime) {
            this.providerDTO = Preconditions.checkNotNull(providerDTO, "providerDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        @Override
        public String getPartitionKey() {
            return providerDTO.getId();
        }
    }

    @Value
    class ProviderDeleted implements ProviderEventPublish {

        public String providerId;
        public Instant eventTime;

        @JsonCreator
        public ProviderDeleted(String providerId, Instant eventTime) {
            this.providerId = Preconditions.checkNotNull(providerId, "providerId");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        @Override
        public String getPartitionKey() {
            return providerId;
        }
    }
}
