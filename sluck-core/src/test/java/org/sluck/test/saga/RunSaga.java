
package org.sluck.test.saga;


import org.sluckframework.cqrs.commandhandling.Command;
import org.sluckframework.cqrs.commandhandling.CommandHandler;
import org.sluckframework.cqrs.commandhandling.disruptor.DisruptorCommandBus;
import org.sluckframework.cqrs.commandhandling.gateway.CommandGateway;
import org.sluckframework.cqrs.commandhandling.gateway.DefaultCommandGateway;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventhanding.SimpleEventBus;
import org.sluckframework.cqrs.eventhanding.scheduling.EventScheduler;
import org.sluckframework.cqrs.eventhanding.scheduling.java.SimpleEventScheduler;
import org.sluckframework.cqrs.saga.GenericSagaFactory;
import org.sluckframework.cqrs.saga.SimpleResourceInjector;
import org.sluckframework.cqrs.saga.annotation.AnnotatedSagaManager;
import org.sluckframework.cqrs.saga.repository.inmemory.InMemorySagaRepository;
import org.sluckframework.cqrs.unitofwork.UnitOfWork;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;
import org.sluckframework.domain.identifier.Identifier;

import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static org.sluckframework.domain.event.aggregate.GenericEvent.asEventMessage;


public class RunSaga {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws InterruptedException {

        // first of all, we need an Event Bus
        EventBus eventBus = new SimpleEventBus();

        // Sagas often need to send commands, so let's create a Command Bus
//        CommandBus commandBus = new SimpleCommandBus();

        AggregateEventStore store = new AggregateEventStore() {

            @Override
            public void appendEvents(String type, AggregateEventStream events) {
                System.out.print("appendEvent");
            }

            @Override
            public AggregateEventStream readEvents(String type, Identifier<?> identifier) {
                System.out.print("readEvent");
                return null;
            }

            @Override
            public AggregateEventStream readEvents(String type, Identifier<?> identifier, long firstSequenceNumber) {
                System.out.print("readEvent");
                return null;
            }

            @Override
            public AggregateEventStream readEvents(String type, Identifier<?> identifier, long firstSequenceNumber, long lastSequenceNumber) {
                System.out.print("readEvent");
                return null;
            }
        };

        DisruptorCommandBus commandBus = new DisruptorCommandBus(store, eventBus);

        // a CommandGateway has a much nicer API
        CommandGateway commandGateway = new DefaultCommandGateway(commandBus);

        // let's register a Command Handler that writes to System Out so we can see what happens
        commandBus.subscribe(MarkToDoItemOverdueCommand.class.getName(),
                new CommandHandler<MarkToDoItemOverdueCommand>() {
                    @Override
                    public Object handle(Command<MarkToDoItemOverdueCommand> commandMessage,
                                         UnitOfWork unitOfWork) throws Throwable {
                        System.out.println(String.format("Got command to mark [%s] overdue!",
                                commandMessage.getPayload().getTodoId()));
                        return null;
                    }
                });

        // The Saga will schedule some deadlines in our sample
        final ScheduledExecutorService executorService = newSingleThreadScheduledExecutor();
        EventScheduler eventScheduler = new SimpleEventScheduler(executorService, eventBus);

        // we need to store a Saga somewhere. Let's do that in memory for now
        InMemorySagaRepository sagaRepository = new InMemorySagaRepository();

        // we want to inject resources in our Saga, so we need to tweak the GenericSagaFactory
        GenericSagaFactory sagaFactory = new GenericSagaFactory();
        // this will allow the eventScheduler and commandGateway to be injected in our Saga
        sagaFactory.setResourceInjector(new SimpleResourceInjector(eventScheduler, commandGateway));

        // Sagas instances are managed and tracked by a SagaManager.
        AnnotatedSagaManager sagaManager = new AnnotatedSagaManager(sagaRepository, sagaFactory, ToDoSaga.class);

        //test with asyn saga
//        AsyncAnnotatedSagaManager sagaManager = new AsyncAnnotatedSagaManager(ToDoSaga.class);
//        sagaManager.start();

        // and we need to subscribe the Saga Manager to the Event Bus
        eventBus.subscribe(sagaManager);
//        sagaManager.subscribe();

        // That's the infrastructure we need...
        // Let's pretend a few things are happening

        // We create 2 items
        eventBus.publish(asEventMessage(new ToDoItemCreatedEvent("todo1", "Got something to do")));
        eventBus.publish(asEventMessage(new ToDoItemCreatedEvent("todo2", "Got something else to do")));
        // We mark the first completed, before the deadline expires. The Saga has a hard-coded deadline of 2 seconds
        eventBus.publish(asEventMessage(new ToDoItemCompletedEvent("todo1")));
        // we wait 3 seconds. Enough time for the deadline to expire
        Thread.sleep(3000);
        // Just a System out to remind us that we should see something
        System.out.println("Should have seen an item marked overdue, now");

        // to make sure the JVM ends, we shut down any threads created by the ExecutorService.
        executorService.shutdown();

        //sagaManager.stop();
        //commandBus.stop();
    }
}
