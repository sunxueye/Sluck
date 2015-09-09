package org.sluckframework.cqrs.commandhandling;

import org.joda.time.DateTime;
import org.sluckframework.domain.identifier.IdentifierFactory;

/**
 * 通用的命令实现
 * 
 * @author sunxy
 * @time 2015年9月7日 上午11:06:30	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class GenericCommand<T> implements Command<T> {

	private static final long serialVersionUID = 6576476272700743359L;

    private final String identifier;
    private final String commandName;
    private final T payload;
    
    private final DateTime dateTime;

    /**
     * 将 用户定义的 command 包装为  框架使用 的 GenericCommand
     *
     * @param command the command to wrap as Command
     * @return a Command containing given <code>command</code> as payload, or <code>command</code> if it already
     *         implements Command.
     */
	public static Command asCommand(Object command) {
        if (Command.class.isInstance(command)) {
            return (Command) command;
        }
        return new GenericCommand<Object>(command);
    }

    /**
     * 使用 payload 创建命令
     *
     * @param payload     the payload for the Message
     */
    public GenericCommand(T payload) {
        this(payload.getClass().getName(), payload);
    }

    /**
     * 使用 给定的 名称 和 payload 创建 命令实例
     *
     * @param commandName The name of the command
     * @param payload     the payload for the Message
     */
    public GenericCommand(String commandName, T payload) {
        this.commandName = commandName;
        this.payload = payload;
        this.identifier = IdentifierFactory.getInstance().generateIdentifier();
        this.dateTime = new DateTime();
    }


    /**
     * 使用给定的参数 初始化
     *
     * @param identifier  the unique identifier of this message
     * @param commandName The name of the command
     * @param payload     the payload for the Message
     */
    public GenericCommand(String identifier, String commandName, T payload) {
        this.identifier = identifier;
        this.commandName = commandName;
        this.payload = payload;
        this.dateTime = new DateTime();
    }

    /**
     * 复制 original 命令属性
     *
     * @param original The original message
     */
    protected GenericCommand(GenericCommand<T> original) {
        this.identifier = original.getIdentifier();
        this.commandName = original.getCommandName();
        this.payload = original.getPayload();
        this.dateTime = original.occurredOn();
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public T getPayload() {
        return payload;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Class getPayloadType() {
        return payload.getClass();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

	@Override
	public DateTime occurredOn() {
		return dateTime;
	}

}
