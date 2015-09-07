package org.sluckframework.cqrs.commandhandling;
/**
 * 命令执行完成后的回调函数
 * 
 * @author sunxy
 * @time 2015年9月7日 上午9:32:10	
 * @since 1.0
 */
public interface CommandCallback<R> {

    /**
     * 当命令执行成功时 执行
     *
     * @param result The result of the command handling execution, if any.
     */
    void onSuccess(R result);

    /**
     * 当失败的时候 执行
     *
     * @param cause The exception raised during command handling
     */
    void onFailure(Throwable cause);

}
