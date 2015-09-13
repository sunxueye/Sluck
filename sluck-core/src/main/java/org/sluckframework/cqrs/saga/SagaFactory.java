package org.sluckframework.cqrs.saga;

/**
 * saga工厂
 *
 * Author: sunxy
 * Created: 2015-09-13 14:44
 * Since: 1.0
 */
public interface SagaFactory {

    /**
     * 使用指定的类型创建新的实例,新的实例必须是已经初始化的,可以使用资源注入注入资源
     *
     * @param sagaType The type of saga to create an instance for
     * @param <T>      The type of saga to create an instance for
     * @return A fully initialized instance of a saga of given type
     */
    <T extends Saga> T createSaga(Class<T> sagaType);

    /**
     * 判断工厂是能创建指定的类型
     *
     * @param sagaType The type of Saga
     * @return <code>true</code> if this factory can create instance of the given <code>sagaType</code>,
     *         <code>false</code> otherwise.
     */
    boolean supports(Class<? extends Saga> sagaType);
}
