package cn.edu.fudan;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * @author XiaoQuanbin
 * @date 2022/5/26
 */
public class OrderModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure(){
        bindService(OrderService.class, OrderServiceImpl.class);
    }
}
