package cn.edu.fudan.lookup;

import akka.Done;
import cn.edu.fudan.service.ServiceConfig;
import cn.edu.fudan.service.ServiceDTO;
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
public class ServiceRepository {
    private final CassandraSession uninitialisedSession;

    /** Will return the session when the Cassandra tables have been successfully created */
    private volatile CompletableFuture<CassandraSession> initialisedSession;

    @Inject
    public ServiceRepository(CassandraSession uninitialisedSession) {
        this.uninitialisedSession = uninitialisedSession;
        // Eagerly create the session
        session();
    }

    private CompletionStage<CassandraSession> session() {
        // If there's no initialised session, or if the initialised session future completed
        // with an exception, then reinitialise the session and attempt to create the tables
        if (initialisedSession == null || initialisedSession.isCompletedExceptionally()) {
            initialisedSession = uninitialisedSession.executeCreateTable(
                    ServiceConfig.CREATE_TABLE_STATEMENT)
                    .thenApply(done -> uninitialisedSession.executeWrite(
                            ServiceConfig.CREATE_TYPE_INDEX_STATEMENT))
                    .thenApply(done -> uninitialisedSession).toCompletableFuture();
        }
        return initialisedSession;
    }

    public CompletionStage<Done> addService(ServiceDTO serviceDTO) {
        return session().thenCompose(session ->
                session.executeWrite(ServiceConfig.INSERT_STATEMENT,
                        serviceDTO.getId(), serviceDTO.getType(),
                        serviceDTO.getProviderId(), serviceDTO.getArea(),
                        serviceDTO.getCost(), serviceDTO.getRating(),
                        serviceDTO.getStatus())
        );
    }

    public CompletionStage<Done> updateService(ServiceDTO serviceDTO) {
        return session().thenCompose(session ->
                session.executeWrite(ServiceConfig.UPDATE_STATEMENT,
                        serviceDTO.getType(), serviceDTO.getProviderId(),
                        serviceDTO.getArea(), serviceDTO.getCost(),
                        serviceDTO.getRating(), serviceDTO.getStatus(),
                        serviceDTO.getId())
        );
    }

    public CompletionStage<Done> deleteService(String id) {
        return session().thenCompose(session ->
                session.executeWrite(ServiceConfig.DELETE_STATEMENT, id)
        );
    }
}
