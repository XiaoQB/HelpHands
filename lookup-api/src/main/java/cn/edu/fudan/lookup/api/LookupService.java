package cn.edu.fudan.lookup.api;

import akka.NotUsed;
import cn.edu.fudan.domain.ProviderDTO;
import cn.edu.fudan.service.ServiceDTO;
import cn.edu.fudan.service.ServiceEventPublish;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PCollection;

import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.Service.topic;

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
    ServiceCall<NotUsed, PCollection<ServiceDTO>> findServiceByType(String type);
    /**
     * router descriptor
     * @return descriptor
     */
    @Override
    default Descriptor descriptor() {
        return Service.named("lookup").withCalls(
                Service.restCall(Method.GET, "/status/service/:type", this::findServiceByType),
                Service.restCall(Method.GET, "/status/provider/:id", this::findProviderById)
        ).withAutoAcl(true);
    }
}
