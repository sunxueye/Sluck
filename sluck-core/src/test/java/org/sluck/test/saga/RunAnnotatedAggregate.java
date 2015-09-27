package org.sluck.test.saga;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.sluckframework.cqrs.commandhandling.CommandBus;
import org.sluckframework.cqrs.commandhandling.SimpleCommandBus;
import org.sluckframework.cqrs.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.sluckframework.cqrs.commandhandling.gateway.CommandGateway;
import org.sluckframework.cqrs.commandhandling.gateway.DefaultCommandGateway;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventhanding.SimpleEventBus;
import org.sluckframework.cqrs.eventhanding.annotation.AnnotationEventListenerAdapter;
import org.sluckframework.cqrs.eventsourcing.EventSourcingRepository;
import org.sluckframework.domain.event.aggregate.AggregateEventStream;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;
import org.sluckframework.domain.identifier.Identifier;
import org.sluckframework.implement.eventstore.jdbc.JdbcEventStore;

import javax.sql.DataSource;

/**
 * Author: sunxy
 * Created: 2015-09-22 23:08
 * Since: 1.0
 */
public class RunAnnotatedAggregate {
    public static void main(String[] args) {
        // let's start with the Command Bus
        CommandBus commandBus = new SimpleCommandBus();

        // the CommandGateway provides a friendlier API to send commands
        CommandGateway commandGateway = new DefaultCommandGateway(commandBus);

        // we'll store Events on the FileSystem, in the "events" folder
//        EventStore eventStore = new FileSystemEventStore(new SimpleEventFileResolver(new File("./events")));

        AggregateEventStore eventStore1 = new AggregateEventStore() {

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

        AggregateEventStore eventStore = new JdbcEventStore(getDataSource());

        // a Simple Event Bus will do
        EventBus eventBus = new SimpleEventBus();

        // we need to configure the repository
        EventSourcingRepository repository = new EventSourcingRepository(ToDoItem.class,
                eventStore);
        repository.setEventBus(eventBus);

        // Axon needs to know that our ToDoItem Aggregate can handle commands
        AggregateAnnotationCommandHandler.subscribe(ToDoItem.class, repository, commandBus);

        // We register an event listener to see which events are created
        AnnotationEventListenerAdapter.subscribe(new ToDoEventHandler(), eventBus);

        // and let's send some Commands on the CommandBus.
        CommandGenerator.sendCommands(commandGateway);
    }

    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(10);
        config.setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
//        config.setJdbcUrl("jdbc:oracle:thin:@169.254.177.142:1521:XE");
        config.addDataSourceProperty("serverName", "169.254.177.142");
        config.addDataSourceProperty("portNumber", "1521");
        config.addDataSourceProperty("databaseName", "XE");
        config.addDataSourceProperty("driverType", "thin");
        config.setUsername("sun");
        config.setPassword("sun");
        config.setConnectionTestQuery("select sysdate from dual");


        return new HikariDataSource(config);
    }
}
