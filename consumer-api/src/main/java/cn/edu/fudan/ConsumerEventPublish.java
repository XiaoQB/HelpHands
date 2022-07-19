package cn.edu.fudan;

import cn.edu.fudan.domain.consumer.ConsumerDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.time.Instant;

/**
 * @author XiaoQuanbin
 * @date 2022/7/19
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ConsumerEventPublish.ConsumerAdded.class, name = "consumer-added"),
        @JsonSubTypes.Type(value = ConsumerEventPublish.ConsumerUpdated.class, name = "consumer-updated"),
        @JsonSubTypes.Type(value = ConsumerEventPublish.ConsumerDeleted.class, name = "consumer-deleted")
})
public interface ConsumerEventPublish {

    /**
     * topic 消息分区字段
     * @return uuid of provider by default
     */
    String getPartitionKey();

    /**
     * An event that represents a change in greeting message.
     */
    @Value
    class ConsumerAdded implements ConsumerEventPublish {

        public ConsumerDTO consumerDTO;
        public Instant eventTime;

        @JsonCreator
        public ConsumerAdded(ConsumerDTO consumerDTO, Instant eventTime) {
            this.consumerDTO = Preconditions.checkNotNull(consumerDTO, "consumerDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        @Override
        public String getPartitionKey() {
            return consumerDTO.getId();
        }
    }

    @Value
    class ConsumerUpdated implements ConsumerEventPublish {

        public ConsumerDTO consumerDTO;
        public Instant eventTime;

        @JsonCreator
        public ConsumerUpdated(ConsumerDTO consumerDTO, Instant eventTime) {
            this.consumerDTO = Preconditions.checkNotNull(consumerDTO, "consumerDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        @Override
        public String getPartitionKey() {
            return consumerDTO.getId();
        }
    }

    @Value
    class ConsumerDeleted implements ConsumerEventPublish {

        public String consumerId;
        public Instant eventTime;

        @JsonCreator
        public ConsumerDeleted(String consumerId, Instant eventTime) {
            this.consumerId = Preconditions.checkNotNull(consumerId, "consumerId");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }

        @Override
        public String getPartitionKey() {
            return consumerId;
        }
    }
}
