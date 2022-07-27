package cn.edu.fudan;

import akka.Done;
import cn.edu.fudan.domain.consumer.ConsumerDTO;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import lombok.extern.slf4j.Slf4j;
import org.pcollections.PSequence;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatements;

/**
 * @author XiaoQuanbin
 * @date 2022/7/26
 */
@Slf4j
public class ConsumerEventProcessor extends ReadSideProcessor<ConsumerEvent> {
    private final CassandraSession session;
    private final CassandraReadSide readSide;


    @Inject
    public ConsumerEventProcessor(CassandraSession cassandraSession, CassandraReadSide readSide) {
        this.session = cassandraSession;
        this.readSide = readSide;
    }


    @Override
    public ReadSideHandler<ConsumerEvent> buildHandler() {
        CassandraReadSide.ReadSideHandlerBuilder<ConsumerEvent> builder =
                readSide.builder(ConsumerConfig.READ_SIDE_ID);

        builder.setGlobalPrepare(this::createTable)
                        .setPrepare(tag -> prepareInsertConsumer());

        builder.setEventHandler(ConsumerEvent.ConsumerAdded.class, this::processConsumerAdded)
                .setEventHandler(ConsumerEvent.ConsumerUpdated.class, this::processConsumerUpdated)
                .setEventHandler(ConsumerEvent.ConsumerUpdated.class, this::processConsumerUpdated);

        return builder.build();
    }

    private CompletionStage<Done> createTable() {
        return session.executeCreateTable(ConsumerConfig.CREATE_TABLE_STATEMENT);
    }

    private PreparedStatement insertConsumer = null;

    private CompletionStage<Done> prepareInsertConsumer() {
        return session
                .prepare(ConsumerConfig.INSERT_STATEMENT)
                .thenApply(
                        ps -> {
                            this.insertConsumer = ps;
                            prepareUpdateConsumer();
                            prepareDeleteConsumer();
                            return Done.getInstance();
                        });
    }

    private PreparedStatement updateConsumer = null;

    private CompletionStage<Done> prepareUpdateConsumer() {
        return session
                .prepare(ConsumerConfig.UPDATE_STATEMENT)
                .thenApply(
                        ps -> {
                            this.updateConsumer = ps;
                            return Done.getInstance();
                        });
    }

    private PreparedStatement deleteConsumer = null;

    private CompletionStage<Done> prepareDeleteConsumer() {
        return session
                .prepare(ConsumerConfig.UPDATE_STATEMENT)
                .thenApply(
                        ps -> {
                            this.deleteConsumer = ps;
                            return Done.getInstance();
                        });
    }


    private CompletionStage<List<BoundStatement>> processConsumerAdded(ConsumerEvent.ConsumerAdded event,
                                                                       Offset offset) {
        log.info("in processConsumerAdded");
        ConsumerDTO consumerToAdd = event.getConsumerDTO();
        BoundStatement bindAddConsumer = insertConsumer.bind(
                consumerToAdd.getId(),
                consumerToAdd.getName(),
                consumerToAdd.getMobile(),
                consumerToAdd.getAddress(),
                consumerToAdd.getEmail(),
                consumerToAdd.getGeo()
        );
        return completedStatements(Arrays.asList(bindAddConsumer));
    }


    private CompletionStage<List<BoundStatement>> processConsumerDeleted(ConsumerEvent.ConsumerDeleted consumerDeleted,
                                                                         Offset offset) {
        BoundStatement bindDeleteConsumer = deleteConsumer.bind(
                consumerDeleted.getConsumerId()
        );
        return completedStatements(Arrays.asList(bindDeleteConsumer));
    }

    private CompletionStage<List<BoundStatement>> processConsumerUpdated(ConsumerEvent.ConsumerUpdated consumerUpdated,
                                                                         Offset offset) {
        ConsumerDTO consumerToUpdate = consumerUpdated.getConsumerDTO();
        BoundStatement bindUpdateConsumer = updateConsumer.bind(
                consumerToUpdate.getName(),
                consumerToUpdate.getMobile(),
                consumerToUpdate.getEmail(),
                consumerToUpdate.getAddress(),
                consumerToUpdate.getGeo(),
                consumerToUpdate.getId()
        );
        return completedStatements(Arrays.asList(bindUpdateConsumer));
    }
    @Override
    public PSequence<AggregateEventTag<ConsumerEvent>> aggregateTags() {
        return ConsumerEvent.TAG.allTags();
    }
}
