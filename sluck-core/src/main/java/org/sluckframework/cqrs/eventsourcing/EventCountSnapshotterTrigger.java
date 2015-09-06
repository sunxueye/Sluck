package org.sluckframework.cqrs.eventsourcing;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.sluckframework.cache.Cache;
import org.sluckframework.common.util.IOUtils;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.cqrs.unitofwork.UnitOfWorkListenerAdapter;
import org.sluckframework.domain.event.aggregate.AggregateEvent;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.identifier.Identifier;

/**
 * 根据聚合事件 的 数量来 触发快照的生成，追踪 未提交聚合事件的 数量，仓储应该使用此装饰器，而避免使用真正的 事件流
 * 
 * @author sunxy
 * @time 2015年9月6日 下午2:14:39	
 * @since 1.0
 */
public class EventCountSnapshotterTrigger implements SnapshotterTrigger {

    private static final int DEFAULT_TRIGGER_VALUE = 50; //默认 追踪 的 未提交的 聚合事件数量

    private Snapshotter snapshotter;
    private final ConcurrentMap<Object, AtomicInteger> counters = new ConcurrentHashMap<Object, AtomicInteger>();
    private volatile boolean clearCountersAfterAppend = true;
    private int trigger = DEFAULT_TRIGGER_VALUE;

    @Override
    public AggregateEventStream decorateForRead(String aggregateType, Object aggregateIdentifier,
                                             AggregateEventStream eventStream) {
        AtomicInteger counter = new AtomicInteger(0);
        counters.put(aggregateIdentifier, counter);
        return new CountingEventStream(eventStream, counter);
    }

    @SuppressWarnings("rawtypes")
	@Override
    public AggregateEventStream decorateForAppend(String aggregateType, EventSourcedAggregateRoot aggregate,
                                               AggregateEventStream eventStream) {
    	Identifier<?> aggregateIdentifier = aggregate.getIdentifier();
        counters.putIfAbsent(aggregateIdentifier, new AtomicInteger(0));
        AtomicInteger counter = counters.get(aggregateIdentifier);
        return new TriggeringEventStream(aggregateType, aggregateIdentifier, eventStream, counter);
    }

    private void triggerSnapshotIfRequired(String type, Identifier<?> aggregateIdentifier,
                                           final AtomicInteger eventCount) {
        if (eventCount.get() > trigger) {
            snapshotter.scheduleSnapshot(type, aggregateIdentifier);
            eventCount.set(1);
        }
    }

    /**
     * 配置 聚合快照的 真正创建者
     *
     * @param snapshotter the snapshotter to notify
     */
    public void setSnapshotter(Snapshotter snapshotter) {
        this.snapshotter = snapshotter;
    }

    /**
     * 设置追踪的聚合未提交事件的数量
     *
     * @param trigger The default trigger value.
     */
    public void setTrigger(int trigger) {
        this.trigger = trigger;
    }

    /**
     * 设置 聚合事件 仓储 存储事件后 是否继续 保存 追踪的聚合未提交事件的数量，默认为true，
     * 当时用缓存的时候，需要设置 false，这样才能避免从 事件仓储中 读取事件
     * 当使用缓存的时候，记得使用{@link
     * #setAggregateCache(org.axonframework.cache.Cache)} or {@link #setAggregateCaches(java.util.List)}
     *
     * @param clearCountersAfterAppend indicator whether to clear counters after appending events
     */
    public void setClearCountersAfterAppend(boolean clearCountersAfterAppend) {
        this.clearCountersAfterAppend = clearCountersAfterAppend;
    }

    /**
     * 为 缓存仓储 设置 缓存
     * 
     * @param cache The cache used by caching repositories
     */
    public void setAggregateCache(Cache cache) {
        this.clearCountersAfterAppend = false;
        cache.registerCacheEntryListener(new CacheListener());
    }

    /**
     * 为 缓存仓储 设置 缓存
     *
     * @param caches The caches used by caching repositories
     */
    public void setAggregateCaches(List<Cache> caches) {
        for (Cache cache : caches) {
            setAggregateCache(cache);
        }
    }

    private class CountingEventStream implements AggregateEventStream, Closeable {

        private final AggregateEventStream delegate;
        private final AtomicInteger counter;

        public CountingEventStream(AggregateEventStream delegate, AtomicInteger counter) {
            this.delegate = delegate;
            this.counter = counter;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @SuppressWarnings("rawtypes")
		@Override
        public AggregateEvent next() {
            AggregateEvent next = delegate.next();
            counter.incrementAndGet();
            return next;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public AggregateEvent peek() {
            return delegate.peek();
        }

        /**
         * Returns the counter containing the number of bytes read.
         *
         * @return the counter containing the number of bytes read
         */
        protected AtomicInteger getCounter() {
            return counter;
        }

        @Override
        public void close() throws IOException {
            IOUtils.closeIfCloseable(delegate);
        }
    }

    private final class TriggeringEventStream extends CountingEventStream {

        private final String aggregateType;
        private final Identifier<?> aggregateIdentifier;

        private TriggeringEventStream(String aggregateType, Identifier<?> aggregateIdentifier,
                                      AggregateEventStream delegate, AtomicInteger counter) {
            super(delegate, counter);
            this.aggregateType = aggregateType;
            this.aggregateIdentifier = aggregateIdentifier;
        }

        @Override
        public boolean hasNext() {
            boolean hasNext = super.hasNext();
            if (!hasNext) {
                CurrentUnitOfWork.get().registerListener(new SnapshotTriggeringListener(aggregateType,
                                                                                        aggregateIdentifier,
                                                                                        getCounter()));
                if (clearCountersAfterAppend) {
                    counters.remove(aggregateIdentifier, getCounter());
                }
            }
            return hasNext;
        }
    }

    private final class CacheListener extends Cache.EntryListenerAdapter {

        @Override
        public void onEntryExpired(Object key) {
            counters.remove(key);
        }

        @Override
        public void onEntryRemoved(Object key) {
            counters.remove(key);
        }
    }

    private class SnapshotTriggeringListener extends UnitOfWorkListenerAdapter {

        private final String aggregateType;
        private final Identifier<?> aggregateIdentifier;
        private final AtomicInteger counter;

        public SnapshotTriggeringListener(String aggregateType,
                                          Identifier<?> aggregateIdentifier, AtomicInteger counter) {
            this.aggregateType = aggregateType;
            this.aggregateIdentifier = aggregateIdentifier;
            this.counter = counter;
        }

        @Override
        public void onCleanup(UnitOfWork unitOfWork) {
            triggerSnapshotIfRequired(aggregateType, aggregateIdentifier, counter);
        }
    }
}
