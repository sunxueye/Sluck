package org.sluckframework.common.serializer;

import static java.lang.String.format;

import org.sluckframework.common.exception.Assert;


/**
 * SerializedObject 简单实现
 * 
 * @author sunxy
 * @time 2015年8月30日 上午2:04:31
 * @since 1.0
 */
public class SimpleSerializedObject<T> implements SerializedObject<T> {

    private final T data;
    private final SerializedType type;
    private final Class<T> dataType;

    /**
     * 使用给定 属性 初始化
     *
     * @param data           The data of the serialized object
     * @param dataType       The type of data
     * @param serializedType The type description of the serialized object
     */
    public SimpleSerializedObject(T data, Class<T> dataType, SerializedType serializedType) {
        Assert.notNull(data, "Data for a serialized object cannot be null");
        Assert.notNull(serializedType, "The type identifier of the serialized object");
        this.data = data;
        this.dataType = dataType;
        this.type = serializedType;
    }

    /**
     * 使用给定的 <code>data</code> and a serialized type identified by given
     * <code>type</code> and <code>revision</code> 初始化
     *
     * @param data     The data of the serialized object
     * @param dataType The type of data
     * @param type     The type identifying the serialized object
     * @param revision The revision of the serialized object
     */
    public SimpleSerializedObject(T data, Class<T> dataType, String type, String revision) {
        this(data, dataType, new SimpleSerializedType(type, revision));
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public Class<T> getContentType() {
        return dataType;
    }

    @Override
    public SerializedType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleSerializedObject<?> that = (SimpleSerializedObject<?>) o;

        if (data != null ? !data.equals(that.data) : that.data != null) {
            return false;
        }
        if (dataType != null ? !dataType.equals(that.dataType) : that.dataType != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = data != null ? data.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return format("SimpleSerializedObject [%s]", type);
    }

}
