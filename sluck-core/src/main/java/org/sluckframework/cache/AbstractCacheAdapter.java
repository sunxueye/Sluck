package org.sluckframework.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 缓存实现的抽象基类
 * 
 * @author sunxy
 * @time 2015年9月5日 下午10:46:18
 * @since 1.0
 */
public abstract class AbstractCacheAdapter<L> implements Cache {

    private final ConcurrentMap<EntryListener, L> registeredAdapters =
            new ConcurrentHashMap<EntryListener, L>();

    protected abstract L createListenerAdapter(EntryListener cacheEntryListener);

    @Override
    public void registerCacheEntryListener(EntryListener entryListener) {
        final L adapter = createListenerAdapter(entryListener);
        if (registeredAdapters.putIfAbsent(entryListener, adapter) == null) {
            doRegisterListener(adapter);
        }
    }

    @Override
    public void unregisterCacheEntryListener(EntryListener entryListener) {
        L adapter = registeredAdapters.remove(entryListener);
        if (adapter != null) {
            doUnregisterListener(adapter);
        }
    }

    /**
     * 移除缓存监听器
     * @param listenerAdapter The listener to register with the cache
     */
    protected abstract void doUnregisterListener(L listenerAdapter);

    /**
     * 注册缓存监听器 
     *
     * @param listenerAdapter the listener to register
     */
    protected abstract void doRegisterListener(L listenerAdapter);

}
