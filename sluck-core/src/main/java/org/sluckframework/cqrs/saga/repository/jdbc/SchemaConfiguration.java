package org.sluckframework.cqrs.saga.repository.jdbc;

/**
 * jdbc saga schema configurate
 *
 * Author: sunxy
 * Created: 2015-09-13 22:23
 * Since: 1.0
 */
public class SchemaConfiguration {

    public static final String DEFAULT_SAGA_ENTRY_TABLE = "SagaEntry";
    public static final String DEFAULT_ASSOC_VALUE_ENTRY_TABLE = "AssociationValueEntry";

    private final String sagaEntryTable;

    private final String assocValueEntryTable;

    public SchemaConfiguration() {
        this(DEFAULT_SAGA_ENTRY_TABLE, DEFAULT_ASSOC_VALUE_ENTRY_TABLE);
    }

    public SchemaConfiguration(String sagaEntryTable, String assocValueEntryTable) {
        this.sagaEntryTable = sagaEntryTable;
        this.assocValueEntryTable = assocValueEntryTable;
    }

    public String assocValueEntryTable() {
        return assocValueEntryTable;
    }

    public String sagaEntryTable() {
        return sagaEntryTable;
    }
}
