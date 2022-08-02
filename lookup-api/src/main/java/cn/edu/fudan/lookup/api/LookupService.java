package cn.edu.fudan.lookup.api;

import akka.NotUsed;
import cn.edu.fudan.domain.ProviderDTO;
import cn.edu.fudan.domain.consumer.ConsumerDTO;
import cn.edu.fudan.domain.order.OrderDTO;
import cn.edu.fudan.service.ServiceDTO;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import java.util.List;


/**
 * @author fuwuchen
 */
public interface LookupService extends Service {
//    GET /lookup/?q=query
//    GET /lookup/?q=query&type=type

    /**
     * Get provider by id
     * @param id uuid of the service
     * @return provider info
     */
    ServiceCall<NotUsed, ProviderDTO> findProviderById(String id);
    /**
     * Get services by type
     * @param type field of service
     * @return list of services
     */
    ServiceCall<NotUsed, List<ServiceDTO>> findServiceByType(String type);
    /**
     * Get all providers (for test)
     * @return list of providers info
     */
    ServiceCall<NotUsed, List<ProviderDTO>> findAllProviders();
    /**
     * Get all services (for test)
     * @return list of services info
     */
    ServiceCall<NotUsed, List<ServiceDTO>> findAllServices();
    /**
     * router descriptor
     * @return descriptor
     */
    @Override
    default Descriptor descriptor() {
        return Service.named("lookup").withCalls(
                Service.restCall(Method.GET, "/status/service/:type", this::findServiceByType),
                Service.restCall(Method.GET, "/status/provider/:id", this::findProviderById),
                Service.restCall(Method.GET, "/status/order/:id", this::findOrderById),
                Service.restCall(Method.GET, "/status/consumer/:id", this::findConsumerById),
                Service.restCall(Method.GET, "/status/service", this::findAllServices),
                Service.restCall(Method.GET, "/status/provider", this::findAllProviders),
                Service.restCall(Method.GET, "/status/consumer", this::findAllConsumers),
                Service.restCall(Method.GET, "/status/order", this::findAllOrders)
        ).withAutoAcl(true);
    }

    ServiceCall<NotUsed, List<OrderDTO>> findAllOrders();

    ServiceCall<NotUsed, List<ConsumerDTO>> findAllConsumers();

    ServiceCall<NotUsed, ConsumerDTO> findConsumerById(String id);

    ServiceCall<NotUsed, OrderDTO> findOrderById(String id);
}
