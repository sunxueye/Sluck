package org.sluckframework.cqrs.saga.annotation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.sluckframework.common.annotation.ClasspathParameterResolverFactory;
import org.sluckframework.common.annotation.ParameterResolverFactory;
import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.cqrs.saga.AssociationValues;
import org.sluckframework.cqrs.saga.Saga;
import org.sluckframework.domain.event.EventProxy;
import org.sluckframework.domain.identifier.IdentifierFactory;

import java.io.Serializable;

/**
 * 基于注解的saga的抽象实现,声明一些事件处理器
 *
 * Author: sunxy
 * Created: 2015-09-14 18:27
 * Since: 1.0
 */
public abstract class AbstractAnnotatedSaga implements Saga, Serializable {

    private static final long serialVersionUID = 3385024168304711298L;

    @JsonIgnore
    private AssociationValues associationValues = new AssociationValuesImpl();

    private final String sagaIdentifier;
    private transient volatile SagaMethodMessageHandlerInspector<? extends AbstractAnnotatedSaga> inspector;
    private volatile boolean active = true;
    private transient ParameterResolverFactory parameterResolverFactory;

    /**
     * 使用默认的 标示符生产器 生产标示符
     */
    protected AbstractAnnotatedSaga() {
        this(IdentifierFactory.getInstance().generateIdentifier());
    }

    /**
     * 使用指定的标示符 初始化
     *
     * @param identifier the identifier to initialize the saga with.
     */
    protected AbstractAnnotatedSaga(String identifier) {
        this.sagaIdentifier = identifier;
        associationValues.add(new AssociationValue("sagaIdentifier", identifier));
    }

    @Override
    public String getSagaIdentifier() {
        return sagaIdentifier;
    }

    @Override
    public AssociationValues getAssociationValues() {
        return associationValues;
    }

    @Override
    public final void handle(EventProxy<?> event) {
        if (active) {
            ensureInspectorInitialized();

            SagaMethodMessageHandler handler = inspector.findHandlerMethod(this, event);
            handler.invoke(this, event);
            if (handler.isEndingHandler()) {
                end();
            }
        }
    }

    private void ensureInspectorInitialized() {
        if (inspector == null) {
            if (parameterResolverFactory == null) {
                parameterResolverFactory = ClasspathParameterResolverFactory.forClass(getClass());
            }
            inspector = SagaMethodMessageHandlerInspector.getInstance(getClass(), parameterResolverFactory);
        }
    }

    /**
     * 配置参数解析工厂
     *
     * @param parameterResolverFactory The parameterResolverFactory for this instance to use
     */
    protected void registerParameterResolverFactory(ParameterResolverFactory parameterResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    /**
     * 标示生命周期的结束
     */
    protected void end() {
        active = false;
    }

    /**
     * 注册关联值
     *
     * @param property The value to associate this saga with.
     */
    @Override
    public void associateWith(AssociationValue property) {
        associationValues.add(property);
    }

    @Override
    public void reSetAssociationValues() {
        associationValues = new AssociationValuesImpl();
    }

    /**
     * 注册关联值
     *
     * @param key   The key of the association value to associate this saga with.
     * @param value The value of the association value to associate this saga with.
     */
    protected void associateWith(String key, String value) {
        associationValues.add(new AssociationValue(key, value));
    }

    /**
     * 注册关联值
     *
     * @param key   The key of the association value to associate this saga with.
     * @param value The value of the association value to associate this saga with.
     */
    protected void associateWith(String key, Number value) {
        associateWith(key, value.toString());
    }

    /**
     * remove 关联值
     *
     * @param property the association value to remove from the saga.
     */
    protected void removeAssociationWith(AssociationValue property) {
        associationValues.remove(property);
    }

    /**
     * remove 关联值
     *
     * @param key   The key of the association value to remove from this saga.
     * @param value The value of the association value to remove from this saga.
     */
    protected void removeAssociationWith(String key, String value) {
        associationValues.remove(new AssociationValue(key, value));
    }

    /**
     * remove 关联值
     *
     * @param key   The key of the association value to remove from this saga.
     * @param value The value of the association value to remove from this saga.
     */
    protected void removeAssociationWith(String key, Number value) {
        removeAssociationWith(key, value.toString());
    }
}