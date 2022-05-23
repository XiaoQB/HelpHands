package cn.edu.fudan.api;

import akka.NotUsed;
import cn.edu.fudan.common.DeleteResult;
import cn.edu.fudan.common.domain.dto.ConsumerDTO;
import cn.edu.fudan.common.domain.param.ConsumerParam;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * @author XiaoQuanbin
 * @date 2022/5/22
 */
public interface ConsumerService extends Service {
    /**
     * Creates a new consumer and returns the consumer ID
     * @return the consumer ID (ServiceComponentDTO)
     */
    ServiceCall<ConsumerParam, ConsumerDTO> add();

    /**
     * Updates the details of an existing consumer
     * @param id service consumer id
     * @return updated ServiceComponentDTO
     */
    ServiceCall<ConsumerParam, ConsumerDTO> updateById(String id);

    /**
     * Gets all the consumers based on request params
     * @return list of ServiceComponentDTO
     */
    ServiceCall<NotUsed, ConsumerDTO> findById(String id);

    /**
     * Deletes the specified consumer
     * @param id service consumer id
     * @return delete status enum
     */
    ServiceCall<NotUsed, DeleteResult<String>> deleteById(String id);

    /**
     * route definition
     * @return descriptor
     */
    @Override
    default Descriptor descriptor() {
        return named("consumer").withCalls(
                restCall(Method.POST, "/consumers", this::add),
                restCall(Method.PUT, "/consumers/:id", this::updateById),
                restCall(Method.GET, "/consumers/:id", this::findById),
                restCall(Method.DELETE, "/consumers/:id", this::deleteById)
        ).withAutoAcl(true);
    }
}
