package org.sluckframework.cqrs.saga;

import java.util.Set;

/**
 * saga仓储,可以通过指定的相关的关联值或标示符找到指定的saga
 *
 * Author: sunxy
 * Created: 2015-09-13 14:54
 * Since: 1.0
 */
public interface SagaRepository {

    /**
     * 找到指定的saga类型和指定的关联值
     *
     * @param type             The type of Saga to return
     * @param associationValue The value that the returned Sagas must be associated with
     * @return A Set containing the found Saga instances. If none are found, an empty Set is returned. Will never
     *         return <code>null</code>.
     */
    Set<String> find(Class<? extends Saga> type, AssociationValue associationValue);

    /**
     * 通过标示符找到指定saga,返回的saga必须是 {@link #commit(Saga) committed}
     *
     * @param sagaIdentifier The unique identifier of the Saga to load
     * @return The Saga instance, or <code>null</code> if no such saga exists
     */
    Saga load(String sagaIdentifier);

    /**
     * 提交change,如果此saga不是激活状态,则删除所有和关联值相关saga
     *
     * @param saga The Saga instance to commit
     */
    void commit(Saga saga);

    /**
     * 再仓储中新增 saga
     *
     * @param saga The Saga instances to add.
     */
    void add(Saga saga);
}
