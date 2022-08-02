package cn.edu.fudan;

import akka.Done;
import cn.edu.fudan.domain.order.OrderDTO;
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
 * @author XiaoQuanbin
 * @date 2022/5/26
 */
@Slf4j
public class OrderEventProcessor extends ReadSideProcessor<OrderEvent> {
    private final CassandraSession session;
    private final CassandraReadSide readSide;

    @Inject
    public OrderEventProcessor(CassandraSession session, CassandraReadSide readSide) {
        this.session = session;
        this.readSide = readSide;
    }

    @Override
    public ReadSideHandler<OrderEvent> buildHandler() {
        CassandraReadSide.ReadSideHandlerBuilder<OrderEvent> builder =
                readSide.builder(OrderConfig.READ_SIDE_ID);

        builder.setGlobalPrepare(this::createTable)
                .setPrepare(tag -> prepareWriteOrder())
        ;
        builder.setEventHandler(OrderEvent.OrderAdded.class, this::processOrderAdded)
                .setEventHandler(OrderEvent.OrderUpdated.class, this::processOrderUpdated)
                .setEventHandler(OrderEvent.OrderDeleted.class, this::processOrderDeleted)
        ;

        return builder.build();
    }

    private CompletionStage<List<BoundStatement>> processOrderDeleted(OrderEvent.OrderDeleted OrderDeleted,
                                                                         Offset offset) {
        BoundStatement bindDeleteOrder = deleteOrder.bind(
                OrderDeleted.getOrderId()
        );
        return completedStatements(Arrays.asList(bindDeleteOrder));
    }

    private CompletionStage<List<BoundStatement>> processOrderUpdated(OrderEvent.OrderUpdated OrderUpdated,
                                                                         Offset offset) {
        OrderDTO OrderToUpdate = OrderUpdated.getOrderDTO();
        BoundStatement bindUpdateOrder = updateOrder.bind(
                OrderToUpdate.getService(),
                OrderToUpdate.getProvider(),
                OrderToUpdate.getConsumer(),
                OrderToUpdate.getCost(),
                OrderToUpdate.getStart(),
                OrderToUpdate.getEnd(),
                OrderToUpdate.getRating(),
                OrderToUpdate.getStatus()
        );
        return completedStatements(Arrays.asList(bindUpdateOrder));
    }

    private CompletionStage<List<BoundStatement>> processOrderAdded(OrderEvent.OrderAdded event,
                                                                       Offset offset) {
        log.info("in processOrderAdded");
        OrderDTO OrderToAdd = event.getOrderDTO();
        BoundStatement bindAddOrder = insertOrder.bind(
                OrderToAdd.getId(),
                OrderToAdd.getService(),
                OrderToAdd.getProvider(),
                OrderToAdd.getConsumer(),
                OrderToAdd.getCost(),
                OrderToAdd.getStart(),
                OrderToAdd.getEnd(),
                OrderToAdd.getRating(),
                OrderToAdd.getStatus()
        );
        return completedStatements(Arrays.asList(bindAddOrder));
    }

    private CompletionStage<Done> createTable() {
        return session.executeCreateTable(OrderConfig.CREATE_TABLE_STATEMENT);
    }

    /** initialized in prepare */
    private PreparedStatement insertOrder = null;

    private CompletionStage<Done> prepareWriteOrder() {
        return session.prepare(OrderConfig.INSERT_STATEMENT)
                .thenApply(ps -> {
                    this.insertOrder = ps;

                    // prepare update statement
                    prepareUpdateOrder();
                    // prepare delete statement
                    prepareDeleteOrder();

                    return Done.getInstance();
                });
    }

    /** initialized in prepare */
    private PreparedStatement updateOrder = null;

    private CompletionStage<Done> prepareUpdateOrder() {
        return session.prepare(OrderConfig.UPDATE_STATEMENT)
                .thenApply(ps -> {
                    this.updateOrder = ps;
                    return Done.getInstance();
                });
    }

    /** initialized in prepare */
    private PreparedStatement deleteOrder = null;

    private CompletionStage<Done> prepareDeleteOrder() {
        return session.prepare(OrderConfig.DELETE_STATEMENT)
                .thenApply(ps -> {
                    this.deleteOrder = ps;
                    return Done.getInstance();
                });
    }

    @Override
    public PSequence<AggregateEventTag<OrderEvent>> aggregateTags() {
        return OrderEvent.TAG.allTags();
    }
}
