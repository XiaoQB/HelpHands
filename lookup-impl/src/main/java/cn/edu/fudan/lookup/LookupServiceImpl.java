package cn.edu.fudan.lookup;

import akka.NotUsed;
import cn.edu.fudan.ConsumerConfig;
import cn.edu.fudan.OrderConfig;
import cn.edu.fudan.domain.ProviderDTO;
import cn.edu.fudan.domain.consumer.ConsumerDTO;
import cn.edu.fudan.domain.order.OrderDTO;
import cn.edu.fudan.lookup.api.LookupService;
import cn.edu.fudan.provider.ProviderConfig;
import cn.edu.fudan.service.ServiceConfig;
import cn.edu.fudan.service.ServiceDTO;
import com.datastax.driver.core.Row;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * @author fuwuchen
 * @date 2022/7/16 15:59
 */
public class LookupServiceImpl implements LookupService {

    private final CassandraSession cassandraSession;


    @Inject
    public LookupServiceImpl(CassandraSession cassandraSession) {
        this.cassandraSession = cassandraSession;
    }
    /**
     * Get provider by id
     *
     * @param id uuid of the service
     * @return provider info
     */
    @Override
    public ServiceCall<NotUsed, ProviderDTO> findProviderById(String id) {
        return request -> {
            CompletionStage<ProviderDTO> summaries = cassandraSession
                    .selectOne(String.format(
                            "SELECT id, name, mobile, since, rating FROM %s where id = ?",
                            ProviderConfig.TABLE_NAME), id)
                    .thenApplyAsync(row -> {
                        Row data = row.orElseThrow(() -> new BadRequest("Provider doesn't exist"));
                        return new ProviderDTO(
                                data.getString("id"),
                                data.getString("name"),
                                data.getString("mobile"),
                                data.getLong("since"),
                                data.getFloat("rating"));
                    });
            return summaries.toCompletableFuture();
        };
    }

    /**
     * Get services by type
     *
     * @param type field of service
     * @return list of services
     */
    @Override
    public ServiceCall<NotUsed, List<ServiceDTO>> findServiceByType(String type) {
        return request -> {
            CompletionStage<List<ServiceDTO>> summaries = cassandraSession
                    .selectAll(ServiceConfig.SELECT_ALL_STATEMENT + "WHERE type = ? ALLOW FILTERING", type)
                    .thenApplyAsync(list -> list.stream()
                            .map(row -> ServiceDTO.builder()
                                    .id(row.getString("id"))
                                    .type(row.getString("type"))
                                    .providerId(row.getString("providerId"))
                                    .area(row.getString("area"))
                                    .cost(row.getInt("cost"))
                                    .rating(row.getInt("rating"))
                                    .status(row.getString("status"))
                                    .build())
                            .collect(Collectors.toList()));
            return summaries.toCompletableFuture();
        };
    }

    /**
     * Get all providers (for test)
     *
     * @return list of providers info
     */
    @Override
    public ServiceCall<NotUsed, List<ProviderDTO>> findAllProviders() {
        return request -> {
            CompletionStage<List<ProviderDTO>> summaries = cassandraSession
                    .selectAll(String.format(
                            "SELECT id, name, mobile, since, rating FROM %s",
                            ProviderConfig.TABLE_NAME))
                    .thenApplyAsync(list -> list.stream()
                            .map(row -> new ProviderDTO(
                                    row.getString("id"),
                                    row.getString("name"),
                                    row.getString("mobile"),
                                    row.getLong("since"),
                                    row.getFloat("rating")))
                            .collect(Collectors.toList()));
            return summaries.toCompletableFuture();
        };
    }

    /**
     * Get all services (for test)
     *
     * @return list of services info
     */
    @Override
    public ServiceCall<NotUsed, List<ServiceDTO>> findAllServices() {
        return request -> {
            CompletionStage<List<ServiceDTO>> summaries = cassandraSession
                    .selectAll(ServiceConfig.SELECT_ALL_STATEMENT)
                    .thenApplyAsync(list -> list.stream()
                            .map(row -> ServiceDTO.builder()
                                            .id(row.getString("id"))
                                            .type(row.getString("type"))
                                            .providerId(row.getString("providerId"))
                                            .area(row.getString("area"))
                                            .cost(row.getInt("cost"))
                                            .rating(row.getInt("rating"))
                                            .status(row.getString("status"))
                                            .build())
                            .collect(Collectors.toList()));
            return summaries.toCompletableFuture();
        };
    }

    @Override
    public ServiceCall<NotUsed, List<OrderDTO>> findAllOrders() {
        return request -> {
            CompletionStage<List<OrderDTO>> summaries =
                    cassandraSession
                            .selectAll(OrderConfig.SELECT_ALL_STATEMENT)
                            .thenApply(list -> list.stream().map(row -> OrderDTO.builder()
                                    .id(row.getString("id"))
                                    .service(row.getString("service"))
                                    .provider(row.getString("provider"))
                                    .consumer(row.getString("consumer"))
                                    .cost(row.getFloat("cost"))
                                    .start(row.getFloat("start"))
                                    .end(row.getFloat("end"))
                                    .rating(row.getFloat("rating"))
                                    .status(row.getString("status"))
                                    .build()
                            ).collect(Collectors.toList()));

            return summaries.toCompletableFuture();
        };
    }

    @Override
    public ServiceCall<NotUsed, List<ConsumerDTO>> findAllConsumers() {
        return request -> {
            CompletionStage<List<ConsumerDTO>> summaries =
                    cassandraSession
                            .selectAll(ConsumerConfig.SELECT_ALL_STATEMENT)
                            .thenApply(list -> list.stream().map(row -> ConsumerDTO.builder()
                                    .id(row.getString("id"))
                                    .name(row.getString("name"))
                                    .address(row.getString("address"))
                                    .mobile(row.getString("mobile"))
                                    .email(row.getString("email"))
                                    .geo(row.getString("geo"))
                                    .build()
                                    ).collect(Collectors.toList()));

            return summaries.toCompletableFuture();
        };
    }

    @Override
    public ServiceCall<NotUsed, ConsumerDTO> findConsumerById(String id) {
        return request -> {
            CompletionStage<ConsumerDTO> summaries = cassandraSession
                    .selectOne(ConsumerConfig.SELECT_ONE_STATEMENT, id)
                    .thenApplyAsync(row -> {
                        Row data = row.orElseThrow(() -> new BadRequest("Consumer doesn't exist"));
                        return new ConsumerDTO(
                                data.getString("id"),
                                data.getString("name"),
                                data.getString("address"),
                                data.getString("mobile"),
                                data.getString("email"),
                                data.getString("geo"));
                    });
            return summaries.toCompletableFuture();
        };
    }

    @Override
    public ServiceCall<NotUsed, OrderDTO> findOrderById(String id) {
        return request -> {
            CompletionStage<OrderDTO> summaries = cassandraSession
                    .selectOne(OrderConfig.SELECT_ONE_STATEMENT, id)
                    .thenApplyAsync(row -> {
                        Row data = row.orElseThrow(() -> new BadRequest("Order doesn't exist"));
                        return new OrderDTO(
                                data.getString("id"),
                                data.getString("service"),
                                data.getString("provider"),
                                data.getString("consumer"),
                                data.getFloat("cost"),
                                data.getFloat("start"),
                                data.getFloat("end"),
                                data.getFloat("rating"),
                                data.getString("status"));
                    });
            return summaries.toCompletableFuture();
        };
    }
}
