package cn.edu.fudan;

import cn.edu.fudan.common.domain.dto.ConsumerDTO;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * @author XiaoQuanbin
 * @date 2022/5/23
 */
public class ConsumerState {
    public final ConsumerDTO consumer;

    @JsonCreator
    ConsumerState(ConsumerDTO consumer) {
        this.consumer = Preconditions.checkNotNull(consumer, "consumer");
    }

    ConsumerState deleteConsumer(String consumerId) {
        if (Objects.equals(consumerId, consumer.getId())) {
            return new ConsumerState(ConsumerDTO.EMPTY);
        }
        return this;
    }

    ConsumerState updateConsumer(ConsumerDTO consumerDTO) {
        return new ConsumerState(consumerDTO);
    }

    boolean hasConsumer(String consumerId) {
        return Objects.equals(consumer.getId(), consumerId);
    }

    public static final ConsumerState EMPTY = new ConsumerState(ConsumerDTO.EMPTY);

    @Override
    public String toString() {
        return "ConsumerState{" +
                "consumers=" + consumer +
                '}';
    }

}
