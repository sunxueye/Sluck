package org.sluckframework.cqrs.saga;

/**
 * saga初始化策略
 *
 * Author: sunxy
 * Created: 2015-09-13 21:18
 * Since: 1.0
 */
public class SagaInitializationPolicy {

    /**
     * 不初始化策略
     */
    public static final SagaInitializationPolicy NONE = new SagaInitializationPolicy(SagaCreationPolicy.NONE, null);

    private final SagaCreationPolicy creationPolicy;
    private final AssociationValue initialAssociationValue;

    /**
     * 使用指定的 创建策略和 关联值 初始化
     *
     * @param creationPolicy          The policy describing the condition to create a new instance
     * @param initialAssociationValue The association value a new Saga instance should be given
     */
    public SagaInitializationPolicy(SagaCreationPolicy creationPolicy, AssociationValue initialAssociationValue) {
        this.creationPolicy = creationPolicy;
        this.initialAssociationValue = initialAssociationValue;
    }

    /**
     * 返回创建策略
     *
     * @return the creation policy
     */
    public SagaCreationPolicy getCreationPolicy() {
        return creationPolicy;
    }

    /**
     * 返回管理值
     *
     * @return the initial association value for a newly created saga
     */
    public AssociationValue getInitialAssociationValue() {
        return initialAssociationValue;
    }
}
