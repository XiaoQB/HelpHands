package cn.edu.fudan.service;

import akka.actor.typed.ActorRef;
import cn.edu.fudan.DeleteStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

/**
 * @author fuwuchen
 * @date 2022/5/19 18:19
 */
public interface ServiceCommand extends Jsonable {

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
    class Add implements ServiceCommand {
        public ServiceParam serviceParam;
        public ActorRef<Confirmation> replyTo;

        @JsonCreator
        Add(ServiceParam serviceParam, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.serviceParam = Preconditions.checkNotNull(
                    serviceParam, "service param cannot be null");
            this.replyTo = replyTo;
        }
    }

    /**
     * Reply type for a create service provider command.
     */
    @Value
    @JsonDeserialize
    class ReplyService implements Accepted<ServiceDTO> {
        public ServiceDTO serviceParam;

        @JsonCreator
        public ReplyService(ServiceDTO serviceParam) {
            this.serviceParam = serviceParam;
        }

        @Override
        public ServiceDTO get() {
            return serviceParam;
        }
    }

    @Value
    @JsonDeserialize
    class FindById implements ServiceCommand {
        public String serviceId;
        public ActorRef<Confirmation> replyTo;

        @JsonCreator
        FindById(String serviceId, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.serviceId = Preconditions.checkNotNull(
                    serviceId, "service param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    class UpdateById implements ServiceCommand {
        public ServiceParam serviceParam;
        public ActorRef<Confirmation> replyTo;

        @JsonCreator
        UpdateById(ServiceParam serviceParam, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.serviceParam = Preconditions.checkNotNull(
                    serviceParam, "service param cannot be null");
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    class DeleteById implements ServiceCommand {
        public String serviceId;
        public ActorRef<Confirmation> replyTo;

        @JsonCreator
        DeleteById(String serviceId, ActorRef<Confirmation> replyTo) {
            // 可以检查参数
            this.serviceId = Preconditions.checkNotNull(
                    serviceId, "service id cannot be null");
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
