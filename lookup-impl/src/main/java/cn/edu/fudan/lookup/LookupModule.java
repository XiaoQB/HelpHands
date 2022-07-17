package cn.edu.fudan.lookup;

import cn.edu.fudan.lookup.api.LookupService;
import cn.edu.fudan.provider.ProviderService;
import cn.edu.fudan.service.ServiceService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * @author fuwuchen
 * @date 2022/5/19 18:13
 */
public class LookupModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(LookupService.class, LookupServiceImpl.class);
        bindClient(ProviderService.class);
        bindClient(ServiceService.class);
        bind(StreamSubscriber.class).asEagerSingleton();
    }
}
