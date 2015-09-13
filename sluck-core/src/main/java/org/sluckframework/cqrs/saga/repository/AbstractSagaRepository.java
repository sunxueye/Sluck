package org.sluckframework.cqrs.saga.repository;

import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.cqrs.saga.AssociationValues;
import org.sluckframework.cqrs.saga.Saga;
import org.sluckframework.cqrs.saga.SagaRepository;

import java.util.Set;

/**
 * 抽象仓储,在JVM总只维护单例的 saga
 *
 * Author: sunxy
 * Created: 2015-09-13 22:08
 * Since: 1.0
 */
public abstract class AbstractSagaRepository implements SagaRepository{

    @Override
    public Set<String> find(Class<? extends Saga> type, AssociationValue associationValue) {
        return findAssociatedSagaIdentifiers(type, associationValue);
    }

    @Override
    public void add(Saga saga) {
        if (saga.isActive()) {
            final String sagaType = typeOf(saga.getClass());
            final AssociationValues associationValues = saga.getAssociationValues();
            for (AssociationValue av : associationValues.addedAssociations()) {
                storeAssociationValue(av, sagaType, saga.getSagaIdentifier());
            }
            associationValues.commit();
            storeSaga(saga);
        }
    }

    @Override
    public void commit(Saga saga) {
        if (!saga.isActive()) {
            deleteSaga(saga);
        } else {
            final String sagaType = typeOf(saga.getClass());
            final AssociationValues associationValues = saga.getAssociationValues();
            for (AssociationValue associationValue : associationValues.addedAssociations()) {
                storeAssociationValue(associationValue, sagaType, saga.getSagaIdentifier());
            }
            for (AssociationValue associationValue : associationValues.removedAssociations()) {
                removeAssociationValue(associationValue, sagaType, saga.getSagaIdentifier());
            }
            associationValues.commit();
            updateSaga(saga);
        }
    }

    /**
     * 找到与指定关联值相关的saga标示符
     *
     * @param type             The type of saga to find identifiers for
     * @param associationValue The value the saga must be associated with
     * @return The identifiers of sagas associated with the given <code>associationValue</code>
     */
    protected abstract Set<String> findAssociatedSagaIdentifiers(Class<? extends Saga> type,
                                                                 AssociationValue associationValue);

    /**
     * 返回指定的 sagaclass的类型标示符
     *
     * @param sagaClass The type of saga to get the type identifier for.
     * @return The type identifier to use for the given sagaClass
     */
    protected abstract String typeOf(Class<? extends Saga> sagaClass);

    /**
     * 删除saga
     *
     * @param saga The saga instance to remove from the repository
     */
    protected abstract void deleteSaga(Saga saga);

    /**
     * 更新saga
     *
     * @param saga The saga that has been modified and needs to be updated in the storage
     */
    protected abstract void updateSaga(Saga saga);

    /**
     * 保存新的saga实例
     *
     * @param saga The newly created Saga instance to store.
     */
    protected abstract void storeSaga(Saga saga);

    /**
     * 保存给定的关联值 和 相关的saga类型和标示符
     *
     * @param associationValue The association value to store
     * @param sagaType         Type type of saga the association value belongs to
     * @param sagaIdentifier   The saga related to the association value
     */
    protected abstract void storeAssociationValue(AssociationValue associationValue,
                                                  String sagaType, String sagaIdentifier);

    /**
     * 删除与指定的saga类型和标示符 相关的 关联值
     *
     * @param associationValue The value to remove as association value for the given saga
     * @param sagaType         The type of the Saga to remove the association from
     * @param sagaIdentifier   The identifier of the Saga to remove the association from
     */
    protected abstract void removeAssociationValue(AssociationValue associationValue,
                                                   String sagaType, String sagaIdentifier);
}
