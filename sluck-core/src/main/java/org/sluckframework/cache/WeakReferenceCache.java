package org.sluckframework.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 简单的弱引用实现， 将一直缓存 value 直到垃圾回收， 不像WeakHashMap， 此缓存 缓存 Value
 * 
 * @author sunxy
 * @time 2015年9月5日 下午11:07:10
 * @since 1.0
 */
public class WeakReferenceCache implements Cache{

    private final ConcurrentMap<Object, Entry> cache = new ConcurrentHashMap<Object, Entry>();
    private final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
    private final Set<EntryListener> adapters = new CopyOnWriteArraySet<EntryListener>();

    @Override
    public void registerCacheEntryListener(EntryListener entryListener) {
        this.adapters.add(entryListener);
    }

    @Override
    public void unregisterCacheEntryListener(EntryListener entryListener) {
        this.adapters.remove(entryListener);
    }

    @SuppressWarnings("unchecked")
	@Override
    public <K, V> V get(K key) {
        purgeItems();
        final Reference<Object> entry = cache.get(key);

        final V returnValue = entry == null ? null : (V) entry.get();
        if (returnValue != null) {
            for (EntryListener adapter : adapters) {
                adapter.onEntryRead(key, returnValue);
            }
        }
        return returnValue;
    }

    @Override
    public <K, V> void put(K key, V value) {
        if (value == null) {
            throw new IllegalArgumentException("Null values not supported");
        }

        purgeItems();
        if (cache.put(key, new Entry(key, value)) != null) {
            for (EntryListener adapter : adapters) {
                adapter.onEntryUpdated(key, value);
            }
        } else {
            for (EntryListener adapter : adapters) {
                adapter.onEntryCreated(key, value);
            }
        }
    }

    @Override
    public <K, V> boolean putIfAbsent(K key, V value) {
        if (value == null) {
            throw new IllegalArgumentException("Null values not supported");
        }
        purgeItems();
        if (cache.putIfAbsent(key, new Entry(key, value)) == null) {
            for (EntryListener adapter : adapters) {
                adapter.onEntryCreated(key, value);
            }
            return true;
        }
        return false;
    }

    @Override
    public <K> boolean remove(K key) {
        if (cache.remove(key) != null) {
            for (EntryListener adapter : adapters) {
                adapter.onEntryRemoved(key);
            }
            return true;
        }
        return false;
    }

    @Override
    public <K> boolean containsKey(K key) {
        purgeItems();
        final Reference<Object> entry = cache.get(key);

        return entry != null && entry.get() != null;
    }

    private void purgeItems() {
        Entry purgedEntry;
        while ((purgedEntry = (Entry) referenceQueue.poll()) != null) {
            if (cache.remove(purgedEntry.getKey()) != null) {
                for (EntryListener adapter : adapters) {
                    adapter.onEntryExpired(purgedEntry.getKey());
                }
            }
        }
    }

    private class Entry extends WeakReference<Object> {

        private final Object key;

        public Entry(Object key, Object value) {
            super(value, referenceQueue);
            this.key = key;
        }

        public Object getKey() {
            return key;
        }
    }

}
