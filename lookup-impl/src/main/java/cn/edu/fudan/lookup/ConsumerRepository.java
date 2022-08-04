package cn.edu.fudan.lookup;

import akka.Done;
import cn.edu.fudan.ConsumerConfig;
import cn.edu.fudan.domain.consumer.ConsumerDTO;
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
public class ConsumerRepository {
    private final CassandraSession uninitialisedSession;

    /** Will return the session when the Cassandra tables have been successfully created */
    private volatile CompletableFuture<CassandraSession> initialisedSession;

    @Inject
    public ConsumerRepository(CassandraSession uninitialisedSession) {
        this.uninitialisedSession = uninitialisedSession;
        // Eagerly create the session
        session();
    }

    private CompletionStage<CassandraSession> session() {
        // If there's no initialised session, or if the initialised session future completed
        // with an exception, then reinitialise the session and attempt to create the tables
        if (initialisedSession == null || initialisedSession.isCompletedExceptionally()) {
            initialisedSession = uninitialisedSession.executeCreateTable(
                    ConsumerConfig.CREATE_TABLE_STATEMENT
            ).thenApply(done -> uninitialisedSession).toCompletableFuture();
        }
        return initialisedSession;
    }

    public CompletionStage<Done> addConsumer(ConsumerDTO consumerDTO) {
        return session().thenCompose(session ->
                session.executeWrite(ConsumerConfig.INSERT_STATEMENT,
                        consumerDTO.getId(), consumerDTO.getName(),
                        consumerDTO.getAddress(), consumerDTO.getMobile(),
                        consumerDTO.getEmail(), consumerDTO.getGeo())
        );
    }


    public CompletionStage<Done> updateConsumer(ConsumerDTO consumerDTO) {
        return session().thenCompose(session ->
                session.executeWrite(ConsumerConfig.UPDATE_STATEMENT,
                        consumerDTO.getId(), consumerDTO.getName(),
                        consumerDTO.getAddress(), consumerDTO.getMobile(),
                        consumerDTO.getEmail(), consumerDTO.getGeo())
        );
    }

    public CompletionStage<Done> deleteConsumer(String id) {
        return session().thenCompose(session ->
                session.executeWrite(ConsumerConfig.DELETE_STATEMENT, id)
        );
    }
}
