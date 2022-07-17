package cn.edu.fudan.lookup;

import akka.Done;
import cn.edu.fudan.provider.ProviderConfig;
import cn.edu.fudan.provider.ProviderDTO;
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
public class ProviderRepository {
    private final CassandraSession uninitialisedSession;

    /** Will return the session when the Cassandra tables have been successfully created */
    private volatile CompletableFuture<CassandraSession> initialisedSession;

    @Inject
    public ProviderRepository(CassandraSession uninitialisedSession) {
        this.uninitialisedSession = uninitialisedSession;
        // Eagerly create the session
        session();
    }

    private CompletionStage<CassandraSession> session() {
        // If there's no initialised session, or if the initialised session future completed
        // with an exception, then reinitialise the session and attempt to create the tables
        if (initialisedSession == null || initialisedSession.isCompletedExceptionally()) {
            initialisedSession = uninitialisedSession.executeCreateTable(
                    ProviderConfig.CREATE_TABLE_STATEMENT
            ).thenApply(done -> uninitialisedSession).toCompletableFuture();
        }
        return initialisedSession;
    }

    public CompletionStage<Done> addProvider(ProviderDTO providerDTO) {
        return session().thenCompose(session ->
                session.executeWrite(ProviderConfig.INSERT_STATEMENT,
                        providerDTO.getId(), providerDTO.getName(),
                        providerDTO.getMobile(), providerDTO.getSince(),
                        providerDTO.getRating())
        );
    }

    public CompletionStage<Done> updateProvider(ProviderDTO providerDTO) {
        return session().thenCompose(session ->
                session.executeWrite(ProviderConfig.UPDATE_STATEMENT,
                        providerDTO.getName(), providerDTO.getMobile(),
                        providerDTO.getSince(), providerDTO.getRating(),
                        providerDTO.getId())
        );
    }

    public CompletionStage<Done> deleteProvider(String id) {
        return session().thenCompose(session ->
                session.executeWrite(ProviderConfig.DELETE_STATEMENT, id)
        );
    }
}
