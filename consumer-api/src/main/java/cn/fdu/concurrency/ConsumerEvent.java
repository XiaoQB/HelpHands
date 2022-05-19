package cn.fdu.concurrency;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author XiaoQuanbin
 * @date 2022/5/19
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
})
public interface ConsumerEvent {
    String getName();
}
