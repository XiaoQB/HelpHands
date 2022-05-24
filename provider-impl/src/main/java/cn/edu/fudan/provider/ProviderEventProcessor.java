package cn.edu.fudan.provider;

import akka.Done;
import cn.edu.fudan.provider.domain.ProviderDTO;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.google.inject.Inject;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.Offset;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import lombok.extern.slf4j.Slf4j;
import org.pcollections.PSequence;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide.completedStatements;

/**
 * @author fuwuchen
 * @date 2022/5/24 16:07
 */
@Slf4j
public class ProviderEventProcessor extends ReadSideProcessor<ProviderEvent> {
    private final CassandraSession session;
    private final CassandraReadSide readSide;

    @Inject
    public ProviderEventProcessor(CassandraSession session, CassandraReadSide readSide) {
        this.session = session;
        this.readSide = readSide;
    }

    @Override
    public ReadSideHandler<ProviderEvent> buildHandler() {
        CassandraReadSide.ReadSideHandlerBuilder<ProviderEvent> builder =
                readSide.builder(ProviderConfig.READ_SIDE_ID);

        builder.setGlobalPrepare(this::createTable)
                .setPrepare(tag -> prepareWriteProvider())
        ;
        builder.setEventHandler(ProviderEvent.ProviderAdded.class, this::processProviderAdded)
                .setEventHandler(ProviderEvent.ProviderUpdated.class, this::processProviderUpdated)
                .setEventHandler(ProviderEvent.ProviderDeleted.class, this::processProviderDeleted)
        ;

        return builder.build();
    }

    private CompletionStage<List<BoundStatement>> processProviderDeleted(ProviderEvent.ProviderDeleted providerDeleted,
                                                                         Offset offset) {
        BoundStatement bindDeleteProvider = deleteProvider.bind(
                providerDeleted.getProviderId()
        );
//        bindDeleteProvider.setString("id", );
        return completedStatements(Arrays.asList(bindDeleteProvider));
    }

    private CompletionStage<List<BoundStatement>> processProviderUpdated(ProviderEvent.ProviderUpdated providerUpdated,
                                                                         Offset offset) {
        ProviderDTO providerToUpdate = providerUpdated.getProviderDTO();
        BoundStatement bindUpdateProvider = updateProvider.bind(
                providerToUpdate.getName(),
                providerToUpdate.getMobile(),
                providerToUpdate.getSince(),
                providerToUpdate.getRating(),
                providerToUpdate.getId()
        );
//        bindUpdateProvider.setString("name", providerToUpdate.getName());
//        bindUpdateProvider.setString("mobile", providerToUpdate.getMobile());
//        bindUpdateProvider.setLong("since", providerToUpdate.getSince());
//        bindUpdateProvider.setFloat("rating", providerToUpdate.getRating());
//        bindUpdateProvider.setString("id", providerToUpdate.getId());
        return completedStatements(Arrays.asList(bindUpdateProvider));
    }

    private CompletionStage<List<BoundStatement>> processProviderAdded(ProviderEvent.ProviderAdded event,
                                                                       Offset offset) {
        log.info("in processProviderAdded");
        ProviderDTO providerToAdd = event.getProviderDTO();
        BoundStatement bindAddProvider = insertProvider.bind(
                providerToAdd.getId(),
                providerToAdd.getName(),
                providerToAdd.getMobile(),
                providerToAdd.getSince(),
                providerToAdd.getRating()
        );
//        bindAddProvider.setString("id", providerToAdd.getId());
//        bindAddProvider.setString("name", providerToAdd.getName());
//        bindAddProvider.setString("mobile", providerToAdd.getMobile());
//        bindAddProvider.setLong("since", providerToAdd.getSince());
//        bindAddProvider.setFloat("rating", providerToAdd.getRating());
        return completedStatements(Arrays.asList(bindAddProvider));
    }

    private CompletionStage<Done> createTable() {
        return session.executeCreateTable(ProviderConfig.CREATE_TABLE_STATEMENT);
    }

    /** initialized in prepare */
    private PreparedStatement insertProvider = null;

    private CompletionStage<Done> prepareWriteProvider() {
        return session.prepare(ProviderConfig.INSERT_STATEMENT)
                .thenApply(ps -> {
                    this.insertProvider = ps;

                    // prepare update statement
                    prepareUpdateProvider();
                    // prepare delete statement
                    prepareDeleteProvider();

                    return Done.getInstance();
                });
    }

    /** initialized in prepare */
    private PreparedStatement updateProvider = null;

    private CompletionStage<Done> prepareUpdateProvider() {
        return session.prepare(ProviderConfig.UPDATE_STATEMENT)
                .thenApply(ps -> {
                    this.updateProvider = ps;
                    return Done.getInstance();
                });
    }

    /** initialized in prepare */
    private PreparedStatement deleteProvider = null;

    private CompletionStage<Done> prepareDeleteProvider() {
        return session.prepare(ProviderConfig.DELETE_STATEMENT)
                .thenApply(ps -> {
                    this.deleteProvider = ps;
                    return Done.getInstance();
                });
    }

    @Override
    public PSequence<AggregateEventTag<ProviderEvent>> aggregateTags() {
        return ProviderEvent.TAG.allTags();
    }
}

