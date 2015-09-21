package org.sluck.test.saga;


import org.sluckframework.cqrs.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.sluckframework.cqrs.commandhandling.disruptor.DisruptorCommandBus;
import org.sluckframework.cqrs.commandhandling.gateway.CommandGateway;
import org.sluckframework.cqrs.commandhandling.gateway.DefaultCommandGateway;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventhanding.SimpleEventBus;
import org.sluckframework.cqrs.eventhanding.annotation.AnnotationEventListenerAdapter;
import org.sluckframework.cqrs.eventsourcing.GenericAggregateFactory;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.domain.repository.Repository;

/**
 * Setting up the basic ToDoItem sample with a disruptor command and event bus and a file based event store. The
 * configuration takes place using spring. We use annotations to find the command and event handlers.
 *
 * @author Allard Buijze
 */
public class RunDisruptorCommandBus {

    public static void main(String[] args) throws InterruptedException {
        // we'll store Events on the FileSystem, in the "events" folder
//        AggregateEventStore eventStore = new FileSystemEventStore(new SimpleEventFileResolver(new File("./events")));

        AggregateEventStore eventStore = new AggregateEventStore() {

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

        // a Simple Event Bus will do
        EventBus eventBus = new SimpleEventBus();

        // we register the event handlers
        AnnotationEventListenerAdapter.subscribe(new ToDoEventHandler(), eventBus);

        // we use default settings for the disruptor command bus
        DisruptorCommandBus commandBus = new DisruptorCommandBus(eventStore, eventBus);

        // now, we obtain a repository from the command bus
        Repository repository = commandBus.createRepository(new GenericAggregateFactory<>(ToDoItem.class));

        // we use the repository to register the command handler
        AggregateAnnotationCommandHandler.subscribe(ToDoItem.class, repository, commandBus);

        // the CommandGateway provides a friendlier API to send commands
        CommandGateway commandGateway = new DefaultCommandGateway(commandBus);

        // and let's send some Commands on the CommandBus.
        CommandGenerator.sendCommands(commandGateway);

        // we need to stop the disruptor command bus, to make sure we release all resources
        commandBus.stop();
    }
}
