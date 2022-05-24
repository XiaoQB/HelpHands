package cn.edu.fudan;

import lombok.Value;

/**
 * @author fuwuchen
 * @date 2022/5/22 09:19
 */
@Value
public class DeleteResult<T> {
    public final DeleteStatus deleteStatus;
    public final T data;

    public DeleteResult(DeleteStatus deleteStatus, T data) {
        this.deleteStatus = deleteStatus;
        this.data = data;
    }
}
