package org.sluckframework.implement.eventstore.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.joda.time.DateTime;
import org.sluckframework.common.serializer.SerializedAggregateEventData;

/**
 * 事件存储 的 sql 执行
 * 
 * @author sunxy
 * @time 2015年8月30日 下午10:17:06
 * @since 1.0
 */
public interface EventSqlSchema<T> {

    /** 
     * 加载最近的快照事件
     * 
     * @param connection
     * @param identifier
     * @param aggregateType
     * @return
     * @throws SQLException
     */
    PreparedStatement sql_loadLastSnapshot(Connection connection, Object identifier, String aggregateType)
            throws SQLException;

    /**
     * 插入领域事件实体
     * 
     */
    PreparedStatement sql_insertDomainEventEntry(Connection connection, String eventIdentifier,
                                                 String aggregateIdentifier, long sequenceNumber,
                                                 DateTime timestamp, String eventType, String eventRevision,
                                                 T eventPayload,
                                                 String aggregateType) throws SQLException;

    /**
     * 插入 快照事件实体
     * 
     * @param connection
     * @param eventIdentifier
     * @param aggregateIdentifier
     * @param sequenceNumber
     * @param timestamp
     * @param eventType
     * @param eventRevision
     * @param eventPayload
     * @param aggregateType
     * @throws SQLException
     */
    PreparedStatement sql_insertSnapshotEventEntry(Connection connection, String eventIdentifier,
                                                   String aggregateIdentifier, long sequenceNumber,
                                                   DateTime timestamp, String eventType, String eventRevision,
                                                   T eventPayload,
                                                   String aggregateType) throws SQLException;

    /**
     * 删除匹配条件的快照
     * 
     * @param connection
     * @param type
     * @param aggregateIdentifier
     * @param sequenceOfFirstSnapshotToPrune
     * @return
     * @throws SQLException
     */
    PreparedStatement sql_pruneSnapshots(Connection connection, String type, Object aggregateIdentifier,
                                         long sequenceOfFirstSnapshotToPrune) throws SQLException;

    /**
     * 找到指定的 sequencenumber 的快照
     * 
     * @param connection
     * @param type
     * @param aggregateIdentifier
     * @return
     * @throws SQLException
     */
    PreparedStatement sql_findSnapshotSequenceNumbers(Connection connection, String type, Object aggregateIdentifier)
            throws SQLException;
    
    /**
     * 根据条件 fentch
     * 
     * @param connection
     * @param type
     * @param aggregateIdentifier
     * @param firstSequenceNumber
     * @return
     * @throws SQLException
     */
    PreparedStatement sql_fetchFromSequenceNumber(Connection connection, String type, Object aggregateIdentifier,
                                                  long firstSequenceNumber) throws SQLException;

    PreparedStatement sql_getFetchAll(Connection connection, String whereClause, Object[] parameters)
            throws SQLException;

    /**
     * 见 快照事件表
     * 
     * @param connection
     * @return
     * @throws SQLException
     */
    PreparedStatement sql_createSnapshotEventEntryTable(Connection connection) throws SQLException;

    /**
     * 建领域实体表
     * 
     * @param connection
     * @return
     * @throws SQLException
     */
    PreparedStatement sql_createDomainEventEntryTable(Connection connection) throws SQLException;

    /**
     * 根据 sql 结果  create 序列化的 聚合事件信息
     * 
     * @param resultSet
     * @return
     * @throws SQLException
     */
    SerializedAggregateEventData<T> createSerializedDomainEventData(ResultSet resultSet) throws SQLException;

    /**
     * 将 date time 转换 为 数据库可识别的 对象
     * 
     * @param input
     * @return
     */
    Object sql_dateTime(DateTime input);

    /**
     * 被序列化存储的 事件类型
     * 
     * @return
     */
    Class<T> getDataType();

}
