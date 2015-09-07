package org.sluckframework.cqrs.commandhandling;


/**
 * 可以从 command 中提取 聚合的 标识符 和版本
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:44:32	
 * @since 1.0
 */
public interface CommandTargetResolver {
	/**
	 * 返回聚合的 标识符 和 聚合的版本，如果没有指定版本 版本可能为null
     *
     * @param command The command from which to extract the identifier and version
     * @return a {@link VersionedAggregateIdentifier} instance reflecting the aggregate to execute the command on
     */
    VersionedAggregateIdentifier resolveTarget(Command<?> command);

}
