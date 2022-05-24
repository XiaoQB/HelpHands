package cn.edu.fudan.provider.api;

import akka.NotUsed;
import cn.edu.fudan.provider.DeleteResult;
import cn.edu.fudan.provider.domain.ProviderDTO;
import cn.edu.fudan.provider.domain.ProviderParam;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * @author fuwuchen
 */
public interface ProviderService extends Service {

    /**
     * Creates a new provider and returns the provider ID
     * @return the provider ID (ServiceComponentDTO)
     */
    ServiceCall<ProviderParam, ProviderDTO> add();

    /**
     * Updates the details of an existing provider
     * @param id service provider id
     * @return updated ServiceComponentDTO
     */
    ServiceCall<ProviderParam, ProviderDTO> updateById(String id);

    /**
     * Adds to the latest ratings for the provider.
     * @param id service provider id
     * @return updated ServiceComponentDTO
     */
    ServiceCall<ProviderParam, ProviderDTO> rateById(String id);

    /**
     * Gets all the providers based on request params
     * @return list of ServiceComponentDTO
     */
    ServiceCall<NotUsed, ProviderDTO> findById(String id);

    /**
     * Deletes the specified provider
     * @param id service provider id
     * @return delete status enum
     */
    ServiceCall<NotUsed, DeleteResult<String>> deleteById(String id);

    /**
     * route definition
     * @return descriptor
     */
    @Override
    default Descriptor descriptor() {
        return Service.named("provider").withCalls(
                Service.restCall(Method.POST, "/providers", this::add),
                Service.restCall(Method.PUT, "/providers/:id", this::updateById),
                Service.restCall(Method.PUT, "/providers/:id/rate", this::rateById),
                Service.restCall(Method.GET, "/providers/:id", this::findById),
                Service.restCall(Method.DELETE, "/providers/:id", this::deleteById)
        ).withAutoAcl(true);
    }
}
