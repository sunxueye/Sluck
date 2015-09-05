package org.sluckframework.common.executor;

import java.util.concurrent.Executor;

/**
 * 简单的执行器
 * 
 * @author sunxy
 * @time 2015年9月6日 上午12:10:32
 * @since 1.0
 */
public final class DirectExecutor implements Executor {

    public static final DirectExecutor INSTANCE = new DirectExecutor();

    @Override
    public void execute(Runnable command) {
        command.run();
    }
}
