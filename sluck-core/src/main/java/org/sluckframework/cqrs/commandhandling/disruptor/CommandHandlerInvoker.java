package org.sluckframework.cqrs.commandhandling.disruptor;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.cache.Cache;
import org.sluckframework.common.exception.Assert;
import org.sluckframework.common.util.IOUtils;
import org.sluckframework.cqrs.eventsourcing.AggregateFactory;
import org.sluckframework.cqrs.eventsourcing.EventSourcedAggregateRoot;
import org.sluckframework.cqrs.eventsourcing.EventStreamDecorator;
import org.sluckframework.cqrs.unitofwork.CurrentUnitOfWork;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;
import org.sluckframework.domain.event.eventstore.EventStreamNotFoundException;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.AggregateNotFoundException;
import org.sluckframework.domain.repository.ConflictingAggregateVersionException;
import org.sluckframework.domain.repository.Repository;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifecycleAware;

/**
 * disputor的 eventHandler 实现
 * 
 * @author sunxy
 * @time 2015年9月7日 下午5:03:38	
 * @since 1.0
 */
@SuppressWarnings("rawtypes")
public class CommandHandlerInvoker implements EventHandler<CommandHandlingEntry>, LifecycleAware {

    private static final Logger logger = LoggerFactory.getLogger(CommandHandlerInvoker.class);
    private static final ThreadLocal<CommandHandlerInvoker> CURRENT_INVOKER = new ThreadLocal<CommandHandlerInvoker>();
    private static final Object PLACEHOLDER_VALUE = new Object();

    private final ConcurrentMap<String, DisruptorRepository> repositories = new ConcurrentHashMap<String, DisruptorRepository>();
    private final Cache cache;
    private final int segmentId;
    private final AggregateEventStore AggregateEventStore;

    /**
     * 使用给定的 AggregateEventStore 和  cache 和 聚合路由 id 初始化 
     *
     * @param AggregateEventStore The event store providing access to events to reconstruct aggregates
     * @param cache      The cache temporarily storing aggregate instances
     * @param segmentId  The id of the segment this invoker should handle
     */
    public CommandHandlerInvoker(AggregateEventStore AggregateEventStore, Cache cache, int segmentId) {
        this.AggregateEventStore = AggregateEventStore;
        this.cache = cache;
        this.segmentId = segmentId;
    }

    /**
     * 根据聚合标识符 找出 对应的仓储
     *
     * @param typeIdentifier The type identifier of the aggregate
     * @param <T>            The type of aggregate
     * @return the repository instance for aggregate of given type
     */
    public static <T extends EventSourcedAggregateRoot> DisruptorRepository getRepository(String typeIdentifier) {
        final CommandHandlerInvoker invoker = CURRENT_INVOKER.get();
        Assert.state(invoker != null, "The repositories of a DisruptorCommandBus are only available "
                + "in the invoker thread");
        return invoker.repositories.get(typeIdentifier);
    }

    @Override
    public void onEvent(CommandHandlingEntry entry, long sequence, boolean endOfBatch) throws Exception {
        if (entry.isRecoverEntry()) {
            removeEntry(entry.getAggregateIdentifier());
        } else if (entry.getInvokerId() == segmentId) {
            DisruptorUnitOfWork unitOfWork = entry.getUnitOfWork();
            unitOfWork.start();
            try {
                Object result = entry.getInvocationInterceptorChain().proceed(entry.getCommand());
                entry.setResult(result);
                unitOfWork.commit();
            } catch (Throwable throwable) {
                entry.setExceptionResult(throwable);
                unitOfWork.rollback(throwable);
            }
        }
    }

    /**
     * 使用指定 的 聚合工厂和 事件流 装饰器 来 创建 仓储
     *
     * @param aggregateFactory The factory creating aggregate instances
     * @param decorator        The decorator to decorate event streams with
     * @param <T>              The type of aggregate created by the factory
     * @return A Repository instance for the given aggregate
     */
    @SuppressWarnings("unchecked")
    public <T extends EventSourcedAggregateRoot> Repository createRepository(AggregateFactory aggregateFactory,
                                                                                EventStreamDecorator decorator) {
        String typeIdentifier = aggregateFactory.getTypeIdentifier();
        if (!repositories.containsKey(typeIdentifier)) {
            DisruptorRepository repository = new DisruptorRepository(aggregateFactory, cache, AggregateEventStore,
                                                                           decorator);
            repositories.putIfAbsent(typeIdentifier, repository);
        }
        return repositories.get(typeIdentifier);
    }

    private void removeEntry(Object aggregateIdentifier) {
        for (DisruptorRepository repository : repositories.values()) {
            repository.removeFromCache(aggregateIdentifier);
        }
        cache.remove(aggregateIdentifier);
    }

    @Override
    public void onStart() {
        CURRENT_INVOKER.set(this);
    }

    @Override
    public void onShutdown() {
        CURRENT_INVOKER.remove();
    }

    /**
     * 被 单例的 CommandHandlerInvoker操作 保证 线程安全
     *
     * @param <T> The type of aggregate stored in this repository
     */
    static final class DisruptorRepository<T extends EventSourcedAggregateRoot<ID>, ID extends Identifier<?>> implements Repository<T, ID> {

        private final AggregateEventStore AggregateEventStore;
        private final EventStreamDecorator decorator;
        private final AggregateFactory<T> aggregateFactory;
        private final Map<T, Object> firstLevelCache = new WeakHashMap<T, Object>();
        private final String typeIdentifier;
        private final Cache cache;

        private DisruptorRepository(AggregateFactory<T> aggregateFactory, Cache cache, AggregateEventStore AggregateEventStore,
                                    EventStreamDecorator decorator) {
            this.aggregateFactory = aggregateFactory;
            this.cache = cache;
            this.AggregateEventStore = AggregateEventStore;
            this.decorator = decorator;
            this.typeIdentifier = this.aggregateFactory.getTypeIdentifier();
        }

        @Override
        public T load(ID aggregateIdentifier, Long expectedVersion) {
            T aggregate = load(aggregateIdentifier);
            if (expectedVersion != null && aggregate.getVersion() > expectedVersion) {
                throw new ConflictingAggregateVersionException(aggregateIdentifier,
                                                               expectedVersion,
                                                               aggregate.getVersion());
            }
            return aggregate;
        }

        @Override
        public T load(ID aggregateIdentifier) {
            T aggregateRoot = null;
            for (T cachedAggregate : firstLevelCache.keySet()) {
                if (aggregateIdentifier.equals(cachedAggregate.getIdentifier())) {
                    logger.debug("Aggregate {} found in first level cache", aggregateIdentifier);
                    aggregateRoot = cachedAggregate;
                }
            }
            if (aggregateRoot == null) {
                Object cachedItem = cache.get(aggregateIdentifier);
                if (cachedItem != null && aggregateFactory.getAggregateType().isInstance(cachedItem)) {
                    aggregateRoot = aggregateFactory.getAggregateType().cast(cachedItem);
                }
            }
            if (aggregateRoot == null) {
                logger.debug("Aggregate {} not in first level cache, loading fresh one from Event Store",
                             aggregateIdentifier);
                AggregateEventStream events = null;
                try {
                    events = decorator.decorateForRead(typeIdentifier, aggregateIdentifier,
                                                       AggregateEventStore.readEvents(typeIdentifier, aggregateIdentifier));
                    if (events.hasNext()) {
                        aggregateRoot = aggregateFactory.createAggregate(aggregateIdentifier, events.peek());
                        aggregateRoot.initializeState(events);
                    }
                } catch (EventStreamNotFoundException e) {
                    throw new AggregateNotFoundException(
                            aggregateIdentifier,
                            "Aggregate not found. Possibly involves an aggregate being created, "
                                    + "or a command that was executed against an aggregate that did not yet "
                                    + "finish the creation process. It will be rescheduled for publication when it "
                                    + "attempts to load an aggregate",
                            e
                    );
                } finally {
                    IOUtils.closeQuietlyIfCloseable(events);
                }
                firstLevelCache.put(aggregateRoot, PLACEHOLDER_VALUE);
                cache.put(aggregateIdentifier, aggregateRoot);
            }
            if (aggregateRoot != null) {
                DisruptorUnitOfWork unitOfWork = (DisruptorUnitOfWork) CurrentUnitOfWork.get();
                unitOfWork.setAggregateType(typeIdentifier);
                unitOfWork.setEventStreamDecorator(decorator);
                unitOfWork.registerAggregate(aggregateRoot, null, null);
            }
            return aggregateRoot;
        }

        @Override
        public void add(T aggregate) {
            DisruptorUnitOfWork unitOfWork = (DisruptorUnitOfWork) CurrentUnitOfWork.get();
            unitOfWork.setEventStreamDecorator(decorator);
            unitOfWork.setAggregateType(typeIdentifier);
            unitOfWork.registerAggregate(aggregate, null, null);
            firstLevelCache.put(aggregate, PLACEHOLDER_VALUE);
            cache.put(aggregate.getIdentifier(), aggregate);
        }

        private void removeFromCache(Object aggregateIdentifier) {
            for (T cachedAggregate : firstLevelCache.keySet()) {
                if (aggregateIdentifier.equals(cachedAggregate.getIdentifier())) {
                    firstLevelCache.remove(cachedAggregate);
                    logger.debug("Aggregate {} removed from first level cache for recovery purposes.",
                                 aggregateIdentifier);
                    return;
                }
            }
        }
    }

}
