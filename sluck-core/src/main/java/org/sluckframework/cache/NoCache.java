package org.sluckframework.cache;


/**
 * 无缓存的默认实现
 * 
 * @author sunxy
 * @time 2015年9月5日 下午11:05:14
 * @since 1.0
 */
public class NoCache implements Cache{

    public static final NoCache INSTANCE = new NoCache();

    private NoCache() {
    }

    @Override
    public <K, V> V get(K key) {
        return null;
    }

    @Override
    public void put(Object key, Object value) {
    }

    @Override
    public boolean putIfAbsent(Object key, Object value) {
        return true;
    }

    @Override
    public boolean remove(Object key) {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public void registerCacheEntryListener(EntryListener cacheEntryListener) {
    }

    @Override
    public void unregisterCacheEntryListener(EntryListener cacheEntryRemovedListener) {
    }

}
