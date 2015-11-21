package org.sluckframework.demo.test.member;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.sluckframework.common.jdbc.DataSourceConnectionProvider;
import org.sluckframework.cqrs.commandhandling.annotation.AggregateAnnotationCommandHandler;
import org.sluckframework.cqrs.commandhandling.disruptor.DisruptorCommandBus;
import org.sluckframework.cqrs.commandhandling.gateway.CommandGateway;
import org.sluckframework.cqrs.commandhandling.gateway.DefaultCommandGateway;
import org.sluckframework.cqrs.eventhanding.EventBus;
import org.sluckframework.cqrs.eventhanding.SimpleEventBus;
import org.sluckframework.cqrs.eventsourcing.GenericAggregateFactory;
import org.sluckframework.cqrs.saga.GenericSagaFactory;
import org.sluckframework.cqrs.saga.ResourceInjector;
import org.sluckframework.cqrs.saga.SimpleResourceInjector;
import org.sluckframework.cqrs.saga.annotation.AnnotatedSagaManager;
import org.sluckframework.cqrs.saga.repository.jdbc.JdbcSagaRepository;
import org.sluckframework.demo.member.Member;
import org.sluckframework.demo.member.Merchant;
import org.sluckframework.demo.test.member.saga.MerchantCreatedSaga;
import org.sluckframework.domain.event.eventstore.AggregateEventStore;
import org.sluckframework.domain.repository.Repository;
import org.sluckframework.implement.eventstore.jdbc.JdbcEventStore;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Author: sunxy
 * Created: 2015-10-08 23:20
 * Since: 1.0
 */
public class MemberTest {
    public static void main(String[] args) throws InterruptedException, SQLException {

        EventBus eventBus = new SimpleEventBus();

        DataSource dataSource = getDataSource();

        AggregateEventStore eventStore = new JdbcEventStore(dataSource);

        // let's start with the Command Bus
        DisruptorCommandBus commandBus = new DisruptorCommandBus(eventStore, eventBus);


        // the CommandGateway provides a friendlier API to send commands
        CommandGateway commandGateway = new DefaultCommandGateway(commandBus);

        // now, we obtain a repository from the command bus
        Repository repository = commandBus.createRepository(new GenericAggregateFactory<>(Member.class));

        Repository repository2 = commandBus.createRepository(new GenericAggregateFactory<>(Merchant.class));

        // Axon needs to know that our ToDoItem Aggregate can handle commands
        AggregateAnnotationCommandHandler.subscribe(Member.class, repository, commandBus);
        AggregateAnnotationCommandHandler.subscribe(Merchant.class, repository2, commandBus);

        // we want to inject resources in our Saga, so we need to tweak the GenericSagaFactory
        GenericSagaFactory sagaFactory = new GenericSagaFactory();
        // this will allow the eventScheduler and commandGateway to be injected in our Saga
        ResourceInjector resourceInjector = new SimpleResourceInjector(commandGateway);
        sagaFactory.setResourceInjector(resourceInjector);

        // Sagas instances are managed and tracked by a SagaManager.
//        AnnotatedSagaManager sagaManager = new AnnotatedSagaManager(sagaRepository, sagaFactory, ToDoSaga.class);

        //test with asyn saga
//        AsyncAnnotatedSagaManager sagaManager = new AsyncAnnotatedSagaManager(MerchantCreatedSaga.class);
//        sagaManager.setSagaFactory(sagaFactory);
//        sagaManager.setProcessorCount(2);
//        sagaManager.start();

        JdbcSagaRepository sagaRepository = new JdbcSagaRepository(new DataSourceConnectionProvider(dataSource));
        sagaRepository.setResourceInjector(resourceInjector);
//        sagaRepository.createSchema();

        AnnotatedSagaManager sagaManager = new AnnotatedSagaManager(sagaRepository, sagaFactory, MerchantCreatedSaga.class);

        // and we need to subscribe the Saga Manager to the Event Bus
        eventBus.subscribe(sagaManager);

        // and let's send some Commands on the CommandBus.
        CommandGenerator.sendCommands(commandGateway);


        Thread.currentThread().sleep(500000);

        commandBus.stop();
//        sagaManager.stop();
        HikariDataSource ds = (HikariDataSource) dataSource;
        ds.close();

    }
    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(10);
        config.setDriverClassName("oracle.jdbc.pool.OracleDataSource");
//        config.setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
//        config.addDataSourceProperty("serverName", "169.254.177.142");
//        config.setJdbcUrl("jdbc:oracle:thin:@169.254.177.142:1521:XE");
        config.setJdbcUrl("jdbc:oracle:thin:@192.168.1.251:1521:newecpss");
//        config.addDataSourceProperty("portNumber", "1521");
//        config.addDataSourceProperty("databaseName", "XE");
//        config.addDataSourceProperty("driverType", "thin");
//        config.setUsername("sun");
        config.setUsername("neika");
//        config.setPassword("sun");
        config.setPassword("neika");
        config.setConnectionTestQuery("select sysdate from dual");
        return new HikariDataSource(config);
    }
}
