package cn.edu.fudan;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import org.pcollections.PSequence;

/**
 * @author XiaoQuanbin
 * @date 2022/5/26
 */
public class OrderEventProcessor extends ReadSideProcessor<OrderEvent> {
    @Override
    public ReadSideHandler<OrderEvent> buildHandler() {
        return null;
    }

    @Override
    public PSequence<AggregateEventTag<OrderEvent>> aggregateTags() {
        return null;
    }
}
