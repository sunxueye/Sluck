package org.sluckframework.cqrs.saga;

/**
 * 资源注入 saga
 *
 * Author: sunxy
 * Created: 2015-09-13 14:37
 * Since: 1.0
 */
public interface ResourceInjector {

    /**
     * 注入资源到给定的 saga 中
     *
     * @param saga The saga to inject resources into
     */
    void injectResources(Saga saga);

}