package org.sluckframework.cqrs.saga.repository;

import org.sluckframework.cache.Cache;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.lock.IdentifierBasedLock;
import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.cqrs.saga.Saga;
import org.sluckframework.cqrs.saga.SagaRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * 支持in-memory的saga仓储
 *
 * Author: sunxy
 * Created: 2015-09-13 22:14
 * Since: 1.0
 */
public class CachingSagaRepository implements SagaRepository {

    private final SagaRepository delegate;
    private final IdentifierBasedLock associationsCacheLock = new IdentifierBasedLock();
    // guarded by "associationsCacheLock"
    private final Cache associationsCache;
    private final Cache sagaCache;

    /**
     * 使用指定的 委派仓储 和 相关值的缓存 saga缓存 初始化
     *
     * @param delegate          The repository instance providing access to (persisted) entries
     * @param associationsCache The cache to store association information is
     * @param sagaCache         The cache to store Saga instances in
     */
    public CachingSagaRepository(SagaRepository delegate, Cache associationsCache, Cache sagaCache) {
        Assert.notNull(delegate, "You must provide a SagaRepository instance to delegate to");
        Assert.notNull(associationsCache, "You must provide a Cache instance to store the association values");
        Assert.notNull(sagaCache, "You must provide a Cache instance to store the sagas");
        this.delegate = delegate;
        this.associationsCache = associationsCache;
        this.sagaCache = sagaCache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> find(Class<? extends Saga> type, AssociationValue associationValue) {
        final String key = cacheKey(associationValue, type.getName());
        // this is a dirty read, but a cache should be thread safe anyway
        Set<String> associations = associationsCache.get(key);
        if (associations == null) {
            associations = feedCache(type, associationValue, key);
        }

        return new HashSet<String>(associations);
    }

    @SuppressWarnings("unchecked")
    private Set<String> feedCache(Class<? extends Saga> type, AssociationValue associationValue, String key) {
        associationsCacheLock.obtainLock(key);
        try {
            Set<String> associations = associationsCache.get(key);
            if (associations == null) {
                associations = delegate.find(type, associationValue);
                associationsCache.put(key, associations);
            }
            return associations;
        } finally {
            associationsCacheLock.releaseLock(key);
        }
    }

    @Override
    public Saga load(String sagaIdentifier) {
        Saga saga = sagaCache.get(sagaIdentifier);
        if (saga == null) {
            saga = delegate.load(sagaIdentifier);
            sagaCache.put(sagaIdentifier, saga);
        }
        return saga;
    }

    @Override
    public void commit(Saga saga) {
        final String sagaIdentifier = saga.getSagaIdentifier();
        sagaCache.put(sagaIdentifier, saga);
        if (saga.isActive()) {
            updateAssociations(saga, sagaIdentifier);
        } else {
            removeCachedAssociations(saga.getAssociationValues(), sagaIdentifier, saga.getClass().getName());
        }
        delegate.commit(saga);
    }

    @Override
    public void add(Saga saga) {
        final String sagaIdentifier = saga.getSagaIdentifier();
        sagaCache.put(sagaIdentifier, saga);
        updateAssociations(saga, sagaIdentifier);
        delegate.add(saga);
    }

    private void updateAssociations(Saga saga, String sagaIdentifier) {
        final String sagaType = saga.getClass().getName();
        addCachedAssociations(saga.getAssociationValues().addedAssociations(), sagaIdentifier, sagaType);
        removeCachedAssociations(saga.getAssociationValues().removedAssociations(), sagaIdentifier, sagaType);
    }

    @SuppressWarnings("unchecked")
    private void addCachedAssociations(Iterable<AssociationValue> associationValues,
                                       String sagaIdentifier, String sagaType) {
        for (AssociationValue associationValue : associationValues) {
            String key = cacheKey(associationValue, sagaType);
            associationsCacheLock.obtainLock(key);
            try {
                Set<String> identifiers = associationsCache.get(key);
                if (identifiers != null && identifiers.add(sagaIdentifier)) {
                    associationsCache.put(key, identifiers);
                }
            } finally {
                associationsCacheLock.releaseLock(key);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void removeCachedAssociations(Iterable<AssociationValue> associationValues,
                                          String sagaIdentifier, String sagaType) {
        for (AssociationValue associationValue : associationValues) {
            String key = cacheKey(associationValue, sagaType);
            associationsCacheLock.obtainLock(key);
            try {
                Set<String> identifiers = associationsCache.get(key);
                if (identifiers != null && identifiers.remove(sagaIdentifier)) {
                    associationsCache.put(key, identifiers);
                }
            } finally {
                associationsCacheLock.releaseLock(key);
            }
        }
    }

    private String cacheKey(AssociationValue associationValue, String sagaType) {
        return sagaType + "/" + associationValue.getKey() + "=" + associationValue.getValue();
    }
}
