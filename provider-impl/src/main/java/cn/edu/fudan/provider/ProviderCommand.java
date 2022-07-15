package cn.edu.fudan.provider;

import akka.actor.typed.ActorRef;
import cn.edu.fudan.DeleteStatus;
import cn.edu.fudan.provider.api.ProviderDTO;
import cn.edu.fudan.provider.api.ProviderParam;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

/**
 * @author fuwuchen
 * @date 2022/5/19 18:19
 */
public interface ProviderCommand extends Jsonable {

    /**
     * Super interface for Accepted/Rejected replies used by Add
     */
    interface Confirmation extends Jsonable {
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
        public String reason;

        @JsonCreator
        public Rejected(String reason) {
            this.reason = reason;
        }
    }

    @Value
    @JsonDeserialize
    class Add implements ProviderCommand {
        public ProviderParam providerParam;
        public ActorRef<Confirmation> replyTo;

        @JsonCreator
        Add(ProviderParam providerParam, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.providerParam = Preconditions.checkNotNull(providerParam, "provider param cannot be null");
            this.replyTo = replyTo;
        }
    }

    /**
     * Reply type for a create service provider command.
     */
    @Value
    @JsonDeserialize
    class ReplyProvider implements Accepted<ProviderDTO> {
        public ProviderDTO providerDto;

        @JsonCreator
        public ReplyProvider(ProviderDTO providerDto) {
            this.providerDto = providerDto;
        }

        @Override
        public ProviderDTO get() {
            return providerDto;
        }
    }

    @Value
    @JsonDeserialize
    class FindById implements ProviderCommand {
        public String providerId;
        public ActorRef<Confirmation> replyTo;

        @JsonCreator
        FindById(String providerId, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.providerId = Preconditions.checkNotNull(providerId, "provider param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    class UpdateById implements ProviderCommand {
        public ProviderParam providerParam;
        public ActorRef<Confirmation> replyTo;

        @JsonCreator
        UpdateById(ProviderParam providerParam, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.providerParam = Preconditions.checkNotNull(providerParam, "provider param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    class DeleteById implements ProviderCommand {
        public String providerId;
        public ActorRef<Confirmation> replyTo;

        @JsonCreator
        DeleteById(String providerId, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.providerId = Preconditions.checkNotNull(providerId, "provider param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    class Deleted implements Accepted<DeleteStatus> {

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
