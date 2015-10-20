package org.sluckframework.cqrs.saga.annotation;

import com.lmax.disruptor.EventFactory;
import org.sluckframework.cqrs.saga.AssociationValue;
import org.sluckframework.cqrs.saga.Saga;
import org.sluckframework.cqrs.saga.SagaCreationPolicy;
import org.sluckframework.domain.event.EventProxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 异步 saga 的 disruptor 事件
 * 
 * Author: sunxy
 * Created: 2015-09-15 21:43
 * Since: 1.0
 */
public class AsyncSagaProcessingEvent {

    private EventProxy<?> publishedEvent;
    private final List<SagaMethodMessageHandler> handlers = new ArrayList<>();
    private Class<? extends AbstractAnnotatedSaga> sagaType;
    private AbstractAnnotatedSaga newSaga;
    private final AsyncSagaCreationElector elector = new AsyncSagaCreationElector();
    private SagaMethodMessageHandler creationHandler;
    private AssociationValue initialAssociationValue;
    private final Set<AssociationValue> associationValues = new HashSet<>();

    /**
     * 返回在 eventBus 上发布的事件,这个事件将触发相应的saga
     *
     * @return the event that has been published on the EventBus
     */
    public EventProxy<?> getPublishedEvent() {
        return publishedEvent;
    }

    /**
     * 方法处理器列表
     *
     * @return the handler that can process the published Event
     */
    public List<SagaMethodMessageHandler> getHandlers() {
        return handlers;
    }

    /**
     * 返回将要处理的 saga 的类型
     *
     * @return the type of Saga being processed
     */
    public Class<? extends Saga> getSagaType() {
        return sagaType;
    }

    /**
     * 强制当前的前程等待创建saga,
     *
     * @param didEventInvocation  indicates whether the current processor found a Saga to process
     * @param processorCount      The total number of processors expected to cast a vote
     * @param ownsNewSagaInstance Indicates whether the current processor "owns" the to-be-created saga instance.
     * @return <code>true</code> if the current processor should create the new instance, <code>false</code> otherwise.
     */
    public boolean waitForSagaCreationVote(boolean didEventInvocation, int processorCount,
                                           boolean ownsNewSagaInstance) {
        return elector.waitForSagaCreationVote(didEventInvocation, processorCount, ownsNewSagaInstance);
    }

    /**
     * 返回新的saga实例
     *
     * @return the new Saga instance
     */
    public AbstractAnnotatedSaga getNewSaga() {
        return newSaga;
    }

    /**
     * reset entry
     *
     * @param nextEvent        The EventProxy<?> to process
     * @param nextSagaType     The type of Saga to process this EventProxy<?>
     * @param nextHandlers     The handlers potentially handling this message
     * @param nextSagaInstance The saga instance to use when a new saga is to be created
     */
    public void reset(EventProxy<?> nextEvent, Class<? extends AbstractAnnotatedSaga> nextSagaType,
                      List<SagaMethodMessageHandler> nextHandlers, AbstractAnnotatedSaga nextSagaInstance) {
        this.elector.clear();
        this.publishedEvent = nextEvent;
        this.sagaType = nextSagaType;
        this.handlers.clear();
        this.handlers.addAll(nextHandlers);
        this.creationHandler = SagaMethodMessageHandler.noHandler();
        this.initialAssociationValue = null;
        this.associationValues.clear();
        for (SagaMethodMessageHandler handler : handlers) {
            if (!this.creationHandler.isHandlerAvailable() && handler.getCreationPolicy() != SagaCreationPolicy.NONE) {
                this.creationHandler = handler;
                this.initialAssociationValue = creationHandler.getAssociationValue(nextEvent);
            }
            this.associationValues.add(handler.getAssociationValue(nextEvent));
        }
        this.newSaga = nextSagaInstance;
    }

    /**
     * 返回基于事件新建saga的方法处理器
     *
     * @return a SagaMethodMessageHandler instance describing the creation rules
     */
    public SagaMethodMessageHandler getCreationHandler() {
        return creationHandler;
    }

    /**
     * 返回新建 saga 事件的关联值,如果事件不是新建 saga 的事件,则返回 null
     *
     * @return the association to assign to an event when handling an incoming event that creates a Saga
     */
    public AssociationValue getInitialAssociationValue() {
        return initialAssociationValue;
    }

    /**
     * 返回所有关联值
     *
     * @return all association values that could potentially link a saga instance with the incoming event
     */
    public Set<AssociationValue> getAssociationValues() {
        return associationValues;
    }

    /**
     * disruptor needed event factory
     */
    static class Factory implements EventFactory<AsyncSagaProcessingEvent> {

        @Override
        public AsyncSagaProcessingEvent newInstance() {
            return new AsyncSagaProcessingEvent();
        }
    }
}