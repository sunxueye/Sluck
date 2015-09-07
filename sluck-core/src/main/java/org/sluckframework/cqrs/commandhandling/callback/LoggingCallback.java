package org.sluckframework.cqrs.commandhandling.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.commandhandling.CommandCallback;

/**
 * 记录日志的 回调 函数
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:10:46	
 * @since 1.0
 */
public class LoggingCallback implements CommandCallback<Object> {

    private static final Logger logger = LoggerFactory.getLogger(LoggingCallback.class);

    private final Command<?> message;

    public LoggingCallback(Command<?> message) {
        this.message = message;
    }

    @Override
    public void onSuccess(Object result) {
        logger.info("Command executed successfully: {}", message.getCommandName());
    }

    @Override
    public void onFailure(Throwable cause) {
        logger.warn("Command resulted in exception: {}", message.getCommandName(), cause);
    }

}
