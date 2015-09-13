package org.sluckframework.cqrs.saga.repository.jpa;

import org.sluckframework.common.serializer.SerializedObject;
import org.sluckframework.common.serializer.Serializer;
import org.sluckframework.common.serializer.SimpleSerializedObject;
import org.sluckframework.cqrs.saga.Saga;

/**
 * saga 实体映射
 *
 * Author: sunxy
 * Created: 2015-09-13 22:30
 * Since: 1.0
 */
public class SagaEntry {

    private String sagaId; // NOSONAR

    private String sagaType;
    private String revision;
    private byte[] serializedSaga;

    private transient Saga saga;

    public SagaEntry(Saga saga, Serializer serializer) {
        this.sagaId = saga.getSagaIdentifier();
        SerializedObject<byte[]> serialized = serializer.serialize(saga, byte[].class);
        this.serializedSaga = serialized.getData();
        this.sagaType = serialized.getType().getName();
        this.revision = serialized.getType().getRevision();
        this.saga = saga;
    }

    public Saga getSaga(Serializer serializer) {
        if (saga != null) {
            return saga;
        }
        return (Saga) serializer.deserialize(new SimpleSerializedObject<byte[]>(serializedSaga, byte[].class,
                sagaType, revision));
    }

    protected SagaEntry() {
        // required by JPA
    }

    public byte[] getSerializedSaga() {
        return serializedSaga; //NOSONAR
    }

    public String getSagaId() {
        return sagaId;
    }

    public String getRevision() {
        return revision;
    }

    public String getSagaType() {
        return sagaType;
    }
}
