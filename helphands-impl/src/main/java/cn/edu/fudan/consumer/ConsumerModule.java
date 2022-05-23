package cn.edu.fudan.consumer;

import cn.edu.fudan.api.ConsumerService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * @author XiaoQuanbin
 * @date 2022/5/23
 */
public class ConsumerModule extends AbstractModule implements ServiceGuiceSupport {
        @Override
        protected void configure() {
            bindService(ConsumerService.class, ConsumerServiceImpl.class);
        }
}

