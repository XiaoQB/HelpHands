package cn.edu.fudan.service;

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
 * @author fuwuchen
 * @date 2022/5/19 18:19
 */
public interface ServiceEvent extends Jsonable, AggregateEvent<ServiceEvent> {
    /**
     * Tags are used for getting and publishing streams of events. Each event
     * will have this tag, and in this case, we are partitioning the tags into
     * 4 shards, which means we can have 4 concurrent processors/publishers of
     * events.
     */
    AggregateEventShards<ServiceEvent> TAG = AggregateEventTag.sharded(
            ServiceEvent.class, 4);

    /**
     * An event that represents a change in greeting message.
     */
    @Value
    @JsonDeserialize
    class ServiceAdded implements ServiceEvent {

        public ServiceDTO serviceDTO;
        public Instant eventTime;

        @JsonCreator
        public ServiceAdded(ServiceDTO serviceDTO, Instant eventTime) {
            this.serviceDTO = Preconditions.checkNotNull(serviceDTO, "serviceDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Value
    @JsonDeserialize
    class ServiceUpdated implements ServiceEvent {

        public ServiceDTO serviceDTO;
        public Instant eventTime;

        @JsonCreator
        public ServiceUpdated(ServiceDTO serviceDTO, Instant eventTime) {
            this.serviceDTO = Preconditions.checkNotNull(serviceDTO, "serviceDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Value
    @JsonDeserialize
    class ServiceDeleted implements ServiceEvent {

        public String serviceId;
        public Instant eventTime;

        @JsonCreator
        public ServiceDeleted(String serviceId, Instant eventTime) {
            this.serviceId = Preconditions.checkNotNull(serviceId, "serviceId");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    /**
     * provide tags for read-side
     * @return AggregateEventShards
     */
    @Override
    default AggregateEventTagger<ServiceEvent> aggregateTag() {
        return TAG;
    }
}
