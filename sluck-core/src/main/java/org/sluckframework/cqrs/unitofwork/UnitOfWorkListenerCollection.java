package org.sluckframework.cqrs.unitofwork;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sluckframework.domain.aggregate.AggregateRoot;
import org.sluckframework.domain.event.EventProxy;

/**
 * uow 集合版 的监听器 ，通知的时候 执行 集合中的 监听器
 * 
 * @author sunxy
 * @time 2015年9月2日 下午2:54:14	
 * @since 1.0
 */
public class UnitOfWorkListenerCollection implements UnitOfWorkListener {

    private static final Logger logger = LoggerFactory.getLogger(UnitOfWorkListenerCollection.class);

    private final Deque<UnitOfWorkListener> listeners = new ArrayDeque<UnitOfWorkListener>();

    /**
     * {@inheritDoc}
     * <p/>
     */
    @Override
    public void afterCommit(UnitOfWork unitOfWork) {
        logger.debug("Notifying listeners after commit");
        Iterator<UnitOfWorkListener> descendingIterator = listeners.descendingIterator();
        while (descendingIterator.hasNext()) {
            UnitOfWorkListener listener = descendingIterator.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Notifying listener [{}] after commit", listener.getClass().getName());
            }
            listener.afterCommit(unitOfWork);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     */
    @Override
    public void onRollback(UnitOfWork unitOfWork, Throwable failureCause) {
        logger.debug("Notifying listeners of rollback");
        Iterator<UnitOfWorkListener> descendingIterator = listeners.descendingIterator();
        while (descendingIterator.hasNext()) {
            UnitOfWorkListener listener = descendingIterator.next();
            if (logger.isDebugEnabled()) {
                logger.debug("Notifying listener [{}] of rollback", listener.getClass().getName());
            }
            listener.onRollback(unitOfWork, failureCause);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     */
    @Override
    public <T> EventProxy<T> onEventRegistered(UnitOfWork unitOfWork, EventProxy<T> event) {
    	EventProxy<T> newEvent = event;
        for (UnitOfWorkListener listener : listeners) {
            newEvent = listener.onEventRegistered(unitOfWork, newEvent);
        }
        return newEvent;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Listeners are called in the order of precedence.
     */
    @SuppressWarnings("rawtypes")
	@Override
    public void onPrepareCommit(UnitOfWork unitOfWork, Set<AggregateRoot> aggregateRoots, List<EventProxy> events) {
        logger.debug("Notifying listeners of commit request");
        for (UnitOfWorkListener listener : listeners) {
            if (logger.isDebugEnabled()) {
                logger.debug("Notifying listener [{}] of upcoming commit", listener.getClass().getName());
            }
            listener.onPrepareCommit(unitOfWork, aggregateRoots, events);
        }
        logger.debug("Listeners successfully notified");
    }

    @Override
    public void onPrepareTransactionCommit(UnitOfWork unitOfWork, Object transaction) {
        logger.debug("Notifying listeners of transaction commit request");
        for (UnitOfWorkListener listener : listeners) {
            if (logger.isDebugEnabled()) {
                logger.debug("Notifying listener [{}] of upcoming transaction commit", listener.getClass().getName());
            }
            listener.onPrepareTransactionCommit(unitOfWork, transaction);
        }
        logger.debug("Listeners successfully notified");
    }

    /**
     * {@inheritDoc}
     * <p/>
     */
    @Override
    public void onCleanup(UnitOfWork unitOfWork) {
        logger.debug("Notifying listeners of cleanup");
        Iterator<UnitOfWorkListener> descendingIterator = listeners.descendingIterator();
        while (descendingIterator.hasNext()) {
            UnitOfWorkListener listener = descendingIterator.next();
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Notifying listener [{}] of cleanup", listener.getClass().getName());
                }
                listener.onCleanup(unitOfWork);
            } catch (RuntimeException e) {
                logger.warn("Listener raised an exception on cleanup. Ignoring...", e);
            }
        }
        logger.debug("Listeners successfully notified");
    }

    /**
     * 增加 监听器 到集合中， 注意 增加的顺序 则为  集合监听器 执行的 时候 的顺序
     *
     * @param listener the listener to be added
     */
    public void add(UnitOfWorkListener listener) {
        if (logger.isDebugEnabled()) {
            logger.debug("Registering listener: {}", listener.getClass().getName());
        }
        listeners.add(listener);
    }
}
