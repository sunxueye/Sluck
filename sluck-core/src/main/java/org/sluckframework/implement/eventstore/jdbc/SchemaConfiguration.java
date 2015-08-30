package org.sluckframework.implement.eventstore.jdbc;
/**
 * schema配置 指定 快照 和 事件 存储
 * 
 * @author sunxy
 * @time 2015年8月30日 下午11:13:56
 * @since 1.0
 */
public class SchemaConfiguration {
	

    public static final String DEFAULT_DOMAINEVENT_TABLE = "DomainEventEntry";
    public static final String DEFAULT_SNAPSHOTEVENT_TABLE = "SnapshotEventEntry";

    private final String eventEntryTable;
    private final String snapshotEntryTable;

    /**
     * 使用默认值 初始化
     */
    public SchemaConfiguration() {
        this(DEFAULT_DOMAINEVENT_TABLE, DEFAULT_SNAPSHOTEVENT_TABLE);
    }

    /**
     * 使用指定的表明初始化
     * 
     * @param eventEntryTable
     * @param snapshotEntryTable
     */
    public SchemaConfiguration(String eventEntryTable, String snapshotEntryTable) {
        this.eventEntryTable = eventEntryTable;
        this.snapshotEntryTable = snapshotEntryTable;
    }

    public String domainEventEntryTable() {
        return eventEntryTable;
    }

    public String snapshotEntryTable() {
        return snapshotEntryTable;
    }

}
