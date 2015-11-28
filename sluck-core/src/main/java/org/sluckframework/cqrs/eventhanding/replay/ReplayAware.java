package org.sluckframework.cqrs.eventhanding.replay;

/**
 * 让事件监听器 基于 事件仓储中的事件 重建状态
 *
 * Author: sunxy
 * Created: 2015-09-21 22:31
 * Since: 1.0
 */
public interface ReplayAware {

    /**
     * 当重建开始时候执行
     */
    void beforeReplay();

    /**
     * 当重建执行完后执行
     */
    void afterReplay();

    /**
     * 当重建失败时 执行
     *
     * @param cause The exception that stopped the replay;
     */
    void onReplayFailed(Throwable cause);

    /**
     * 是否已经重建
     * @return if replay
     */
    boolean alreadyReplay();
}
