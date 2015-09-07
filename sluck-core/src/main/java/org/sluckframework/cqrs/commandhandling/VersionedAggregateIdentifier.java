package org.sluckframework.cqrs.commandhandling;

import org.sluckframework.domain.identifier.Identifier;

/**
 * 值对象，包含聚合标识符和 版本
 * 
 * @author sunxy
 * @time 2015年9月7日 上午10:46:08	
 * @since 1.0
 */
public class VersionedAggregateIdentifier {

    private final Identifier<?> identifier;
    private final Long version;

    /**
     * 使用指定 参数 初始化
     *
     * @param identifier The identifier of the targeted aggregate
     * @param version    The expected version of the targeted aggregate, or {@code null} if the version is irrelevant
     */
    public VersionedAggregateIdentifier(Identifier<?> identifier, Long version) {
        this.identifier = identifier;
        this.version = version;
    }

    /**
     * 返回聚合标识符
     *
     * @return the identifier of the targeted Aggregate
     */
    public Identifier<?> getIdentifier() {
        return identifier;
    }

    /**
     * 返回聚合版本
     *
     * @return the version of the targeted Aggregate
     */
    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        VersionedAggregateIdentifier that = (VersionedAggregateIdentifier) o;

        if (!identifier.equals(that.identifier)) {
            return false;
        }
        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = identifier.hashCode();
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

}
