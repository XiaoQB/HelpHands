package cn.edu.fudan.provider.api;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import cn.edu.fudan.DeleteResult;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
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
     * Gets the provider by id
     * @param id uuid of provider
     * @return list of ServiceComponentDTO
     */
    ServiceCall<NotUsed, ProviderDTO> findById(String id);

    /**
     * Gets all the providers
     * @return list of providers
     */
    ServiceCall<NotUsed, Source<ProviderDTO, ?>> findAll();

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
        return named("provider").withCalls(
                restCall(Method.POST, "/providers", this::add),
                restCall(Method.PUT, "/providers/:id", this::updateById),
                restCall(Method.PUT, "/providers/:id/rate", this::rateById),
                restCall(Method.GET, "/providers/:id", this::findById),
                restCall(Method.DELETE, "/providers/:id", this::deleteById),
                restCall(Method.GET, "/providers", this::findAll)
        ).withAutoAcl(true);
    }
}
