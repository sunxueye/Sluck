package org.sluckframework.cqrs.saga;

import java.util.Set;

/**
 * 表示一个单例 saga 所有关联值列表
 *
 * Author: sunxy
 * Created: 2015-09-11 19:58
 * Since: 1.0
 */
public interface AssociationValues extends Iterable<AssociationValue> {

    /**
     * 返回从最后一次 commit() 的所有被remove 的关联值,如果没有提交,则不返回
     *
     * @return the Set of association values removed since the last {@link #commit()}.
     */
    Set<AssociationValue> removedAssociations();

    /**
     * 返回从最后一次 commit() 的所有被 add 的关联值,如果没有提交,则不返回
     *
     * @return the Set of association values added since the last {@link #commit()}.
     */
    Set<AssociationValue> addedAssociations();

    /**
     * 重置 tracked changes
     */
    void commit();

    /**
     * 返回size
     *
     * @return the number of AssociationValue instances available
     */
    int size();

    /**
     * 判断当前saga实例是否包含指定的 关联值
     *
     * @param associationValue the association value to verify
     * @return <code>true</code> if the association value is available in this instance, otherwise <code>false</code>
     */
    boolean contains(AssociationValue associationValue);

    /**
     * 如果之前没有加入此关联值,则加入
     *
     * @param associationValue The association value to add
     * @return <code>true</code> if the value was added, <code>false</code> if it was already contained in this
     *         instance
     */
    boolean add(AssociationValue associationValue);

    /**
     * 移除指定的关联值
     *
     * @param associationValue The association value to remove
     * @return <code>true</code> if the value was removed, <code>false</code> if it was not contained in this instance
     */
    boolean remove(AssociationValue associationValue);

    /**
     * 返回只读的 关联值 set
     *
     * @return a read only view on the contents of this container
     */
    Set<AssociationValue> asSet();
}