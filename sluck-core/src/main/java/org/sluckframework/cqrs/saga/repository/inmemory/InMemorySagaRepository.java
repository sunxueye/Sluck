package org.sluckframework.cqrs.saga.repository.inmemory;

import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.cqrs.saga.Saga;
import org.sluckframework.cqrs.saga.SagaRepository;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于内存的saga仓储
 *
 * Author: sunxy
 * Created: 2015-09-13 21:53
 * Since: 1.0
 */
public class InMemorySagaRepository implements SagaRepository {

    private final ConcurrentMap<String, Saga> managedSagas = new ConcurrentHashMap<String, Saga>();

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> find(Class<? extends Saga> type, AssociationValue associationValue) {
        Set<String> result = new TreeSet<String>();
        for (Saga saga : managedSagas.values()) {
            if (saga.getAssociationValues().contains(associationValue) && type.isInstance(saga)) {
                result.add(saga.getSagaIdentifier());
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Saga load(String sagaIdentifier) {
        return managedSagas.get(sagaIdentifier);
    }

    @Override
    public void commit(Saga saga) {
        if (!saga.isActive()) {
            managedSagas.remove(saga.getSagaIdentifier());
        } else {
            managedSagas.put(saga.getSagaIdentifier(), saga);
        }
        saga.getAssociationValues().commit();
    }

    @Override
    public void add(Saga saga) {
        commit(saga);
    }

    public int size() {
        return managedSagas.size();
    }
}
