package org.sluckframework.cache;

/**
 * 缓存 Key - value
 * 
 * @author sunxy
 * @time 2015年9月5日 下午10:37:30
 * @since 1.0
 */
public interface Cache {

    /**
     * 根据Key  返回 cache value，如果没有，返回null
     * 
     * @param key The key under which the item was cached
     * @param <K> The type of key used
     * @param <V> The type of value stored
     * @return the item stored under the given key
     */
    <K, V> V get(K key);

    /**
     * 保存  cache
     *
     * @param key   The key under which to store the item
     * @param value The item to cache
     * @param <K>   The type of key used
     * @param <V>   The type of value stored
     */
    <K, V> void put(K key, V value);

    /**
     * 没有就Put 返回true, 有就不put,返回false，需要保证是原子化的操作
     *
     * @param key   The key under which to store the item
     * @param value The item to cache
     * @param <K>   The type of key used
     * @param <V>   The type of value stored
     * @return <code>true</code> if no value was previously assigned to the key, <code>false</code> otherwise.
     */
    <K, V> boolean putIfAbsent(K key, V value);

    /**
     * 移除缓存，如果不存在则不做实际操作
     *
     * @param key The key under which the item was stored
     * @param <K> The type of key used
     * @return <code>true</code> if a value was previously assigned to the key and has been removed, <code>false</code>
     * otherwise.
     */
    <K> boolean remove(K key);

    /**
     * 是否 包含指定缓存
     *
     * @param key The key to check
     * @param <K> The type of key
     * @return <code>true</code> if an item is available under that key, <code>false</code> otherwise.
     */
    <K> boolean containsKey(K key);

    /**
     * 注册缓存监听器，监听缓存的变化
     * Registers the given <code>cacheEntryListener</code> to listen for Cache changes.
     *
     * @param cacheEntryListener The listener to register
     */
    void registerCacheEntryListener(EntryListener cacheEntryListener);

    /**
     * 移除缓存监听器
     *
     * @param cacheEntryListener The listener to unregister
     */
    void unregisterCacheEntryListener(EntryListener cacheEntryListener);

    /**
     * 定义的缓存监听器，监听缓存变化
     */
    interface EntryListener {

        /**
         * 当缓存中实体过期的时候执行
         *
         * @param key The key of the entry that expired
         */
        void onEntryExpired(Object key);

        /**
         * 当缓存中实体被移除的时候执行
         *
         * @param key The key of the entry that was removed
         */
        void onEntryRemoved(Object key);

        /**
         * 当缓存实体 被更新的时候执行
         *
         * @param key   The key of the entry that was updated
         * @param value The new value of the entry
         */
        void onEntryUpdated(Object key, Object value);

        /**
         * 当一个新的缓存实体 被加入的到时候 执行
         *
         * @param key   The key of the entry that was added
         * @param value The value of the entry
         */
        void onEntryCreated(Object key, Object value);

        /**
         * 当缓存 中的 实体 被读取的时候执行
         *
         * @param key   The key of the entry that was read
         * @param value The value of the entry read
         */
        void onEntryRead(Object key, Object value);

        /**
         * 对缓存进行clone,必须实现 ava.lang.Cloneable 接口
         *
         * @return a copy of this instance
         */
        Object clone() throws CloneNotSupportedException;
    }

    /**
     * 缓存监听器的 适配器， 方便子类实现
     */
    class EntryListenerAdapter implements EntryListener {

        @Override
        public void onEntryExpired(Object key) {
        }

        @Override
        public void onEntryRemoved(Object key) {
        }

        @Override
        public void onEntryUpdated(Object key, Object value) {
        }

        @Override
        public void onEntryCreated(Object key, Object value) {
        }

        @Override
        public void onEntryRead(Object key, Object value) {
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

}
