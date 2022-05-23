package cn.edu.fudan.consumer;

import akka.actor.typed.ActorRef;
import cn.edu.fudan.common.DeleteStatus;
import cn.edu.fudan.common.domain.dto.ConsumerDTO;
import cn.edu.fudan.common.domain.dto.ProviderDTO;
import cn.edu.fudan.common.domain.param.ConsumerParam;
import cn.edu.fudan.common.domain.param.ConsumerParam;
import cn.edu.fudan.consumer.ConsumerCommand;
import cn.edu.fudan.provider.ProviderCommand;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

/**
 * @author XiaoQuanbin
 * @date 2022/5/23
 */
public interface ConsumerCommand extends Jsonable {
    /**
     * Super interface for Accepted/Rejected replies used by Add
     */
    interface Confirmation {
    }


    interface Accepted<T> extends Confirmation {
        /**
         * 获取正常流程的返回值
         * @return 由 Command 决定 Reply 类型
         */
        T get();
    }

    @Value
    @JsonDeserialize
    class Rejected implements Confirmation {
        public final String reason;

        @JsonCreator
        public Rejected(String reason) {
            this.reason = reason;
        }
    }


    @Value
    @JsonDeserialize
    final class Add implements ConsumerCommand {
        public final ConsumerParam consumerParam;
        public final ActorRef<Confirmation> replyTo;

        @JsonCreator
        Add(ConsumerParam consumerParam, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.consumerParam = Preconditions.checkNotNull(consumerParam, "consumer param cannot be null");
            this.replyTo = replyTo;
        }
    }

    /**
     * Reply type for a create service consumer command.
     */
    /**
     * Reply type for a create service provider command.
     */
    @Value
    @JsonDeserialize
    class ReplyConsumer implements ConsumerCommand.Accepted<ConsumerDTO> {
        public final ConsumerDTO consumerDTO;

        @JsonCreator
        public ReplyConsumer(ConsumerDTO consumerDTO) {
            this.consumerDTO = consumerDTO;
        }

        @Override
        public ConsumerDTO get() {
            return consumerDTO;
        }
    }



    @Value
    @JsonDeserialize
    final class FindById implements ConsumerCommand {
        public final String consumerId;
        public final ActorRef<Confirmation> replyTo;

        @JsonCreator
        FindById(String consumerId, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.consumerId = Preconditions.checkNotNull(consumerId, "consumer param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    final class UpdateById implements ConsumerCommand {
        public final ConsumerParam consumerParam;
        public final ActorRef<Confirmation> replyTo;

        @JsonCreator
        UpdateById(ConsumerParam consumerParam, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.consumerParam = Preconditions.checkNotNull(consumerParam, "consumer param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    final class DeleteById implements ConsumerCommand {
        public final String consumerId;
        public final ActorRef<Confirmation> replyTo;

        @JsonCreator
        DeleteById(String consumerId, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.consumerId = Preconditions.checkNotNull(consumerId, "consumer param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    final class Deleted implements ConsumerCommand.Accepted<DeleteStatus> {

        public DeleteStatus status;

        @JsonCreator
        public Deleted(DeleteStatus status) {
            this.status = status;
        }

        @Override
        public DeleteStatus get() {
            return status;
        }
    }

}
