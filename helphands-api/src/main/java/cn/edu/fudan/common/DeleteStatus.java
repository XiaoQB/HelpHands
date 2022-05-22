package cn.edu.fudan.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author fuwuchen
 */
public enum DeleteStatus {
    /**
     * 删除成功
     */
    SUCCESS("SUCCESS"),
    /**
     * 删除失败
     */
    FAILED("FAILED")
    ;

    private final String code;

    @JsonCreator
    DeleteStatus(String code) {
        this.code = code;
    }

    @JsonValue
    public String getValue() {
        return code;
    }
}
