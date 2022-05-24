package cn.edu.fudan;

import cn.edu.fudan.domain.consumer.ConsumerDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.time.Instant;

/**
 * @author XiaoQuanbin
 * @date 2022/5/23
 */
public interface ConsumerEvent extends Jsonable, AggregateEvent<ConsumerEvent> {
    /**
     * Tags are used for getting and publishing streams of events. Each event
     * will have this tag, and in this case, we are partitioning the tags into
     * 4 shards, which means we can have 4 concurrent processors/publishers of
     * events.
     */
    AggregateEventShards<ConsumerEvent> TAG = AggregateEventTag.sharded(ConsumerEvent.class, 4);

    /**
     * An event that represents a change in greeting message.
     */
    @Value
    @JsonDeserialize
    class ConsumerAdded implements ConsumerEvent {

        public ConsumerDTO consumerDTO;
        public Instant eventTime;

        @JsonCreator
        public ConsumerAdded(ConsumerDTO consumerDTO, Instant eventTime) {
            this.consumerDTO = Preconditions.checkNotNull(consumerDTO, "consumerDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Value
    @JsonDeserialize
    class ConsumerUpdated implements ConsumerEvent {

        public ConsumerDTO consumerDTO;
        public Instant eventTime;

        @JsonCreator
        public ConsumerUpdated(ConsumerDTO consumerDTO, Instant eventTime) {
            this.consumerDTO = Preconditions.checkNotNull(consumerDTO, "consumerDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Value
    @JsonDeserialize
    class ConsumerDeleted implements ConsumerEvent {

        public String consumerId;
        public Instant eventTime;

        @JsonCreator
        public ConsumerDeleted(String consumerId, Instant eventTime) {
            this.consumerId = Preconditions.checkNotNull(consumerId, "consumerId");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Override
    default AggregateEventTagger<ConsumerEvent> aggregateTag() {
        return TAG;
    }
}
