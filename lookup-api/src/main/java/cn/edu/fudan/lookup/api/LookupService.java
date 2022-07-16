package cn.edu.fudan.lookup.api;

import akka.NotUsed;
import cn.edu.fudan.service.ServiceDTO;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PCollection;

import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * @author fuwuchen
 */
public interface LookupService extends Service {
//    GET /lookup/?q=query
//    GET /lookup/?q=query&type=type
//    GET /status/provider/:id
//    GET /status/service/:type

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
                Service.restCall(Method.GET, "/status/service/:type", this::findServiceByType)
        ).withAutoAcl(true);
    }
}
