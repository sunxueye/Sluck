package org.sluckframework.cqrs.commandhandling.annotation;

import org.sluckframework.common.annotation.AbstractMessageHandler;

/**
 * 命令处理工具
 *
 * Author: sunxy
 * Created: 2015-09-10 11:14
 * Since: 1.0
 */
public abstract class CommandMessageHandlerUtils {

    /**
     * 返回指定的命令处理器处理命令的名称
     *
     * @param handler The handler to resolve the name from
     * @return The name of the command accepted by the handler
     */
    public static String resolveAcceptedCommandName(AbstractMessageHandler handler) {
        CommandHandler annotation = handler.getAnnotation(CommandHandler.class);
        if (annotation != null && !"".equals(annotation.commandName())) {
            return annotation.commandName();
        }
        return handler.getPayloadType().getName();
    }
}
