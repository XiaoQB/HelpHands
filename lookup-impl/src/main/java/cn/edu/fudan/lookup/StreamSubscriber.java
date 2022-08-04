package cn.edu.fudan.lookup;

import akka.Done;
import akka.stream.javadsl.Flow;
import cn.edu.fudan.ConsumerEventPublish;
import cn.edu.fudan.ConsumerService;
import cn.edu.fudan.OrderEventPublish;
import cn.edu.fudan.OrderService;
import cn.edu.fudan.provider.ProviderService;
import cn.edu.fudan.provider.ProviderEventPublish;
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
                            ServiceRepository serviceRepository,
                            OrderService orderService,
                            OrderRepository orderRepository,
                            ConsumerService consumerService,
                            ConsumerRepository consumerRepository) {
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

        orderService.orderEvent().subscribe()
                .withGroupId("lookup-order")
                // And subscribe to it with at least once processing semantics.
                .atLeastOnce(
                        // Create a flow that emits a Done for each message it processes
                        Flow.<OrderEventPublish>create().mapAsync(1, event -> {
                            if (event instanceof OrderEventPublish.OrderAdded) {
                                OrderEventPublish.OrderAdded messageChanged =
                                        (OrderEventPublish.OrderAdded) event;
                                return orderRepository.addOrder(messageChanged.getOrderDTO());
                            } else if (event instanceof OrderEventPublish.OrderUpdated) {
                                OrderEventPublish.OrderUpdated messageChanged =
                                        (OrderEventPublish.OrderUpdated) event;
                                return orderRepository.updateOrder(messageChanged.getOrderDTO());
                            } else if (event instanceof OrderEventPublish.OrderDeleted) {
                                OrderEventPublish.OrderDeleted messageChanged =
                                        (OrderEventPublish.OrderDeleted) event;
                                return orderRepository.deleteOrder(messageChanged.getOrderId());
                            } else {
                                // Ignore all others events
                                return CompletableFuture.completedFuture(Done.getInstance());
                            }
                        })
                );

        consumerService.consumerEvent().subscribe()
                .withGroupId("lookup-consumer")
                // And subscribe to it with at least once processing semantics.
                .atLeastOnce(
                        // Create a flow that emits a Done for each message it processes
                        Flow.<ConsumerEventPublish>create().mapAsync(1, event -> {
                            if (event instanceof ConsumerEventPublish.ConsumerAdded) {
                                ConsumerEventPublish.ConsumerAdded messageChanged =
                                        (ConsumerEventPublish.ConsumerAdded) event;
                                return consumerRepository.addConsumer(messageChanged.getConsumerDTO());
                            } else if (event instanceof ConsumerEventPublish.ConsumerUpdated) {
                                ConsumerEventPublish.ConsumerUpdated messageChanged =
                                        (ConsumerEventPublish.ConsumerUpdated) event;
                                return consumerRepository.updateConsumer(messageChanged.getConsumerDTO());
                            } else if (event instanceof ConsumerEventPublish.ConsumerDeleted) {
                                ConsumerEventPublish.ConsumerDeleted messageChanged =
                                        (ConsumerEventPublish.ConsumerDeleted) event;
                                return consumerRepository.deleteConsumer(messageChanged.getConsumerId());
                            } else {
                                // Ignore all others events
                                return CompletableFuture.completedFuture(Done.getInstance());
                            }
                        })
                );

    }
}
