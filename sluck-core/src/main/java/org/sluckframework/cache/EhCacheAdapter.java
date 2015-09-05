package org.sluckframework.cache;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

/**
 * 基于 Ehcache 的实现
 * 
 * @author sunxy
 * @time 2015年9月5日 下午10:49:41
 * @since 1.0
 */
public class EhCacheAdapter extends AbstractCacheAdapter<CacheEventListener> {

    private final Ehcache ehCache;

    /**
     * 使用给定的 Ehcache 缓存初始化
     *
     * @param ehCache The cache instance to forward calls to
     */
    public EhCacheAdapter(Ehcache ehCache) {
        this.ehCache = ehCache;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> V get(K key) {
        final Element element = ehCache.get(key);
        return element == null ? null : (V) element.getObjectValue();
    }

    @Override
    public <K, V> void put(K key, V value) {
        ehCache.put(new Element(key, value));
    }

    @Override
    public <K, V> boolean putIfAbsent(K key, V value) {
        return ehCache.putIfAbsent(new Element(key, value)) == null;
    }

    @Override
    public <K> boolean remove(K key) {
        return ehCache.remove(key);
    }

    @Override
    public <K> boolean containsKey(K key) {
        return ehCache.isKeyInCache(key);
    }

    @Override
    protected EhCacheAdapter.CacheEventListenerAdapter createListenerAdapter(EntryListener cacheEntryListener) {
        return new EhCacheAdapter.CacheEventListenerAdapter(ehCache, cacheEntryListener);
    }

    @Override
    protected void doUnregisterListener(CacheEventListener listenerAdapter) {
        ehCache.getCacheEventNotificationService().unregisterListener(listenerAdapter);
    }

    @Override
    protected void doRegisterListener(CacheEventListener listenerAdapter) {
        ehCache.getCacheEventNotificationService().registerListener(listenerAdapter);
    }

    private static class CacheEventListenerAdapter implements CacheEventListener, Cloneable {

        private Ehcache ehCache;
        private EntryListener delegate;

        public CacheEventListenerAdapter(Ehcache ehCache, EntryListener delegate) {
            this.ehCache = ehCache;
            this.delegate = delegate;
        }

        @Override
        public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
            if (cache.equals(ehCache)) {
                delegate.onEntryRemoved(element.getObjectKey());
            }
        }

        @Override
        public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
            if (cache.equals(ehCache)) {
                delegate.onEntryCreated(element.getObjectKey(), element.getObjectValue());
            }
        }

        @Override
        public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
            if (cache.equals(ehCache)) {
                delegate.onEntryUpdated(element.getObjectKey(), element.getObjectValue());
            }
        }

        @Override
        public void notifyElementExpired(Ehcache cache, Element element) {
            if (cache.equals(ehCache)) {
                delegate.onEntryExpired(element.getObjectKey());
            }
        }

        @Override
        public void notifyElementEvicted(Ehcache cache, Element element) {
            if (cache.equals(ehCache)) {
                delegate.onEntryExpired(element.getObjectKey());
            }
        }

        @Override
        public void notifyRemoveAll(Ehcache cache) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public CacheEventListenerAdapter clone() throws CloneNotSupportedException {
            CacheEventListenerAdapter clone = (CacheEventListenerAdapter) super.clone();
            clone.ehCache = (Ehcache) ehCache.clone();
            clone.delegate = (EntryListener) delegate.clone();
            return clone;
        }
    }

}
