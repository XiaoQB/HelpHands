package cn.edu.fudan.lookup;

import akka.NotUsed;
import cn.edu.fudan.lookup.api.LookupService;
import cn.edu.fudan.service.ServiceDTO;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PCollection;

/**
 * @author fuwuchen
 * @date 2022/7/16 15:59
 */
public class LookupServiceImpl implements LookupService {
    /**
     * Get services by type
     *
     * @param type field of service
     * @return list of services
     */
    @Override
    public ServiceCall<NotUsed, PCollection<ServiceDTO>> findServiceByType(String type) {
        return null;
    }
}
