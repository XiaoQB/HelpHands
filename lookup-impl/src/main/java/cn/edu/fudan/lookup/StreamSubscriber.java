package cn.edu.fudan.lookup;

import akka.Done;
import akka.stream.javadsl.Flow;
import cn.edu.fudan.provider.api.ProviderService;
import cn.edu.fudan.provider.domain.ProviderEventPublish;
import cn.edu.fudan.service.ServiceEventPublish;
import cn.edu.fudan.service.ServiceService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * @author fuwuchen
 * @date 2022/7/16 17:42
 */
public class StreamSubscriber {
    @Inject
    public StreamSubscriber(ProviderService providerService,
                            ProviderRepository providerRepository,
                            ServiceService serviceService,
                            ServiceRepository serviceRepository) {
        // Create a subscriber
        providerService.providerEvent().subscribe()
                .withGroupId("lookup-provider")
                // And subscribe to it with at least once processing semantics.
                .atLeastOnce(
                        // Create a flow that emits a Done for each message it processes
                        Flow.<ProviderEventPublish>create().mapAsync(1, event -> {
                            if (event instanceof ProviderEventPublish.ProviderAdded) {
                                ProviderEventPublish.ProviderAdded messageChanged =
                                        (ProviderEventPublish.ProviderAdded) event;
                                return providerRepository.addProvider(messageChanged.getProviderDTO());
                            } else if (event instanceof ProviderEventPublish.ProviderUpdated) {
                                ProviderEventPublish.ProviderUpdated messageChanged =
                                        (ProviderEventPublish.ProviderUpdated) event;
                                return providerRepository.updateProvider(messageChanged.getProviderDTO());
                            } else if (event instanceof ProviderEventPublish.ProviderDeleted) {
                                ProviderEventPublish.ProviderDeleted messageChanged =
                                        (ProviderEventPublish.ProviderDeleted) event;
                                return providerRepository.deleteProvider(messageChanged.getProviderId());
                            } else {
                                // Ignore all others events
                                return CompletableFuture.completedFuture(Done.getInstance());
                            }
                        })
                );

        serviceService.serviceEvent().subscribe()
                .withGroupId("lookup-service")
                // And subscribe to it with at least once processing semantics.
                .atLeastOnce(
                        // Create a flow that emits a Done for each message it processes
                        Flow.<ServiceEventPublish>create().mapAsync(1, event -> {
                            if (event instanceof ServiceEventPublish.ServiceAdded) {
                                ServiceEventPublish.ServiceAdded messageChanged =
                                        (ServiceEventPublish.ServiceAdded) event;
                                return serviceRepository.addService(messageChanged.getServiceDTO());
                            } else if (event instanceof ServiceEventPublish.ServiceUpdated) {
                                ServiceEventPublish.ServiceUpdated messageChanged =
                                        (ServiceEventPublish.ServiceUpdated) event;
                                return serviceRepository.updateService(messageChanged.getServiceDTO());
                            } else if (event instanceof ServiceEventPublish.ServiceDeleted) {
                                ServiceEventPublish.ServiceDeleted messageChanged =
                                        (ServiceEventPublish.ServiceDeleted) event;
                                return serviceRepository.deleteService(messageChanged.getServiceId());
                            } else {
                                // Ignore all others events
                                return CompletableFuture.completedFuture(Done.getInstance());
                            }
                        })
                );
    }
}
