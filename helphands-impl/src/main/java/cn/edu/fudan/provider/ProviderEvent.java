package cn.edu.fudan.provider;

import cn.edu.fudan.domain.ProviderDTO;
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
public interface ProviderEvent extends Jsonable, AggregateEvent<ProviderEvent> {
    /**
     * Tags are used for getting and publishing streams of events. Each event
     * will have this tag, and in this case, we are partitioning the tags into
     * 4 shards, which means we can have 4 concurrent processors/publishers of
     * events.
     */
    AggregateEventShards<ProviderEvent> TAG = AggregateEventTag.sharded(ProviderEvent.class, 4);

    /**
     * An event that represents a change in greeting message.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class ProviderAdded implements ProviderEvent {

        public final ProviderDTO providerDTO;
        public final Instant eventTime;

        @JsonCreator
        public ProviderAdded(ProviderDTO providerDTO, Instant eventTime) {
            this.providerDTO = Preconditions.checkNotNull(providerDTO, "providerDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Value
    @JsonDeserialize
    final class ProviderUpdated implements ProviderEvent {

        public final cn.edu.fudan.domain.ProviderDTO providerDTO;
        public final Instant eventTime;

        @JsonCreator
        public ProviderUpdated(cn.edu.fudan.domain.ProviderDTO providerDTO, Instant eventTime) {
            this.providerDTO = Preconditions.checkNotNull(providerDTO, "providerDTO");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Value
    @JsonDeserialize
    final class ProviderDeleted implements ProviderEvent {

        public final String providerId;
        public final Instant eventTime;

        @JsonCreator
        public ProviderDeleted(String providerId, Instant eventTime) {
            this.providerId = Preconditions.checkNotNull(providerId, "providerId");
            this.eventTime = Preconditions.checkNotNull(eventTime, "eventTime");
        }
    }

    @Override
    default AggregateEventTagger<ProviderEvent> aggregateTag() {
        return TAG;
    }
}
