package cn.edu.fudan.lookup;

import akka.Done;
import cn.edu.fudan.OrderConfig;
import cn.edu.fudan.domain.order.OrderDTO;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author fuwuchen
 * @date 2022/7/16 17:43
 */
@Singleton
public class OrderRepository {
    private final CassandraSession uninitialisedSession;

    /** Will return the session when the Cassandra tables have been successfully created */
    private volatile CompletableFuture<CassandraSession> initialisedSession;

    @Inject
    public OrderRepository(CassandraSession uninitialisedSession) {
        this.uninitialisedSession = uninitialisedSession;
        // Eagerly create the session
        session();
    }

    private CompletionStage<CassandraSession> session() {
        // If there's no initialised session, or if the initialised session future completed
        // with an exception, then reinitialise the session and attempt to create the tables
        if (initialisedSession == null || initialisedSession.isCompletedExceptionally()) {
            initialisedSession = uninitialisedSession.executeCreateTable(
                    OrderConfig.CREATE_TABLE_STATEMENT
            ).thenApply(done -> uninitialisedSession).toCompletableFuture();
        }
        return initialisedSession;
    }

    public CompletionStage<Done> addOrder(OrderDTO orderDTO) {
        return session().thenCompose(session ->
                session.executeWrite(OrderConfig.INSERT_STATEMENT,
                        orderDTO.getId(), orderDTO.getService(),
                        orderDTO.getProvider(), orderDTO.getConsumer(),
                        orderDTO.getCost(), orderDTO.getStart(), orderDTO.getEnd(),
                        orderDTO.getRating(), orderDTO.getStatus())
        );
    }


    public CompletionStage<Done> updateOrder(OrderDTO orderDTO) {
        return session().thenCompose(session ->
                session.executeWrite(OrderConfig.UPDATE_STATEMENT,
                        orderDTO.getProvider(),
                        orderDTO.getStart(), orderDTO.getEnd(), orderDTO.getCost(),
                        orderDTO.getRating(), orderDTO.getStatus(), orderDTO.getId(), orderDTO.getConsumer())
        );
    }

    public CompletionStage<Done> deleteOrder(String id) {
        return session().thenCompose(session ->
                session.executeWrite(OrderConfig.DELETE_STATEMENT, id)
        );
    }
}
