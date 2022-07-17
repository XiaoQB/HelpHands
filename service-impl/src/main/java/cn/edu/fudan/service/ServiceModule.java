package cn.edu.fudan.service;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * @author fuwuchen
 * @date 2022/5/19 18:13
 */
public class ServiceModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(ServiceService.class, ServiceServiceImpl.class);
    }
}
