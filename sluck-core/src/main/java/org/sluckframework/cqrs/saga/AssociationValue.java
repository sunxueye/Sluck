package org.sluckframework.cqrs.saga;

import org.sluckframework.common.exception.Assert;

import java.io.Serializable;

/**
 * saga关联的属性,用于找到指定的saga
 *
 * Author: sunxy
 * Created: 2015-09-11 19:51
 * Since: 1.0
 */
public class AssociationValue implements Serializable {

    private static final long serialVersionUID = 3573690125021875389L;

    private String key;
    private String value;

    private AssociationValue(){}

    /**
     * 使用指定的key和value创建关联值
     *
     * @param key   The key of the Association Value. Usually indicates where the value comes from.
     * @param value The value corresponding to the key of the association. It is highly recommended to only use
     *              serializable values.
     */
    public AssociationValue(String key, String value) {
        Assert.notNull(key, "Cannot associate a Saga with a null key");
        Assert.notNull(value, "Cannot associate a Saga with a null value");
        this.key = key;
        this.value = value;
    }

    /**
     * 返回key, key一般用于标示值的来源
     *
     * @return the key of this association value
     */
    public String getKey() {
        return key;
    }

    /**
     * 返回关联的值
     *
     * @return the value of this association. Never <code>null</code>.
     */
    public String getValue() {
        return value;
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AssociationValue that = (AssociationValue) o;

        if (!key.equals(that.key)) {
            return false;
        }
        if (!value.equals(that.value)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }


}