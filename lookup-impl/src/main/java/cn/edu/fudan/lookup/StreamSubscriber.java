package cn.edu.fudan.lookup;

import akka.Done;
import akka.stream.javadsl.Flow;
import cn.edu.fudan.provider.api.ProviderService;
import cn.edu.fudan.provider.domain.ProviderEventPublish;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

/**
 * @author fuwuchen
 * @date 2022/7/16 17:42
 */
public class StreamSubscriber {
    @Inject
    public StreamSubscriber(ProviderService providerService,
                            ProviderRepository providerRepository) {
        // Create a subscriber
        providerService.providerEvent().subscribe()
                .withGroupId("lookup")
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
    }
}
