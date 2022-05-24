package cn.edu.fudan.provider.api;

import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * @author fuwuchen
 * @date 2022/5/19 14:06
 */
public interface ServiceComponentService extends Service {
    /**
     * Creates a new service and returns the service ID
     * @return the service ID
     */
    ServiceCall create();

    /**
     * Updates the details of an existing service
     * @return
     */
    ServiceCall updateById();

    /**
     * Increments the stars for the service by one
     * @return
     */
    ServiceCall rateById();

    /**
     * Gets all the services based on request params
     * @return
     */
    ServiceCall findById();

    /**
     * Deletes the specified service
     * @return
     */
    ServiceCall deleteById();

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return Service.named("service").withCalls(
                Service.restCall(Method.POST, "/services",  this::create),
                Service.restCall(Method.PUT, "/services/:id", this::updateById),
                Service.restCall(Method.PUT, "/services/:id/rate", this::rateById),
                Service.restCall(Method.GET, "/services", this::findById),
                Service.restCall(Method.DELETE, "/services/:id", this::deleteById)
        ).withAutoAcl(true);
        // @formatter:on
    }
}
