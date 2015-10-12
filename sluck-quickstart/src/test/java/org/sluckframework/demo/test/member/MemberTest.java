package org.sluckframework.demo.test.member;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.sluckframework.cqrs.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.sluckframework.cqrs.commandhandling.disruptor.DisruptorCommandBus;
import org.sluckframework.cqrs.commandhandling.gateway.CommandGateway;
import org.sluckframework.cqrs.commandhandling.gateway.DefaultCommandGateway;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventhanding.SimpleEventBus;
import org.sluckframework.cqrs.eventsourcing.GenericAggregateFactory;
import org.sluckframework.demo.member.Member;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;
import org.sluckframework.domain.repository.Repository;
import org.sluckframework.implement.eventstore.jdbc.JdbcEventStore;

import javax.sql.DataSource;

/**
 * Author: sunxy
 * Created: 2015-10-08 23:20
 * Since: 1.0
 */
public class MemberTest {
    public static void main(String[] args) {

        EventBus eventBus = new SimpleEventBus();

        DataSource dataSource = getDataSource();

        AggregateEventStore eventStore = new JdbcEventStore(dataSource);
        // let's start with the Command Bus
        DisruptorCommandBus commandBus = new DisruptorCommandBus(eventStore, eventBus);

        // the CommandGateway provides a friendlier API to send commands
        CommandGateway commandGateway = new DefaultCommandGateway(commandBus);

        // now, we obtain a repository from the command bus
        Repository repository = commandBus.createRepository(new GenericAggregateFactory<>(Member.class));

        // Axon needs to know that our ToDoItem Aggregate can handle commands
        AggregateAnnotationCommandHandler.subscribe(Member.class, repository, commandBus);

        // and let's send some Commands on the CommandBus.
        CommandGenerator.sendCommands(commandGateway);

        commandBus.stop();
        HikariDataSource ds = (HikariDataSource) dataSource;
        ds.close();

    }
    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(10);
        config.setDriverClassName("oracle.jdbc.pool.OracleDataSource");
//        config.setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
        config.setJdbcUrl("jdbc:oracle:thin:@169.254.177.142:1521:XE");
//        config.addDataSourceProperty("serverName", "169.254.177.142");
//        config.addDataSourceProperty("portNumber", "1521");
//        config.addDataSourceProperty("databaseName", "XE");
//        config.addDataSourceProperty("driverType", "thin");
        config.setUsername("sun");
        config.setPassword("sun");
        config.setConnectionTestQuery("select sysdate from dual");
        return new HikariDataSource(config);
    }
}
