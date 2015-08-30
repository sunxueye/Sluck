package org.sluckframework.domain.event.eventstore.query;


/**
 * 根据属性构建查询条件
 * <p/>
 * <em>Example:</em><br/>
 * <pre>
 *     CriteriaBuilder entry = eventStore.newCriteriaBuilder();
 *     Criteria criteria = entry.property("timeStamp").greaterThan("2015-08-29");
 *     eventStore.visitEvents(criteria, visitor);
 * </pre>
 * 
 * @author sunxy
 * @time 2015年8月29日 下午4:18:47
 * @since 1.0
 */
public interface CriteriaBuilder {
	
	/**
	 * 返回 事件仓储中 的对应的 属性对象实例，可以用这个属性对象创建相应的条件实例，这个属性对象对应的仓储中的属性需要带有索引
     *
     * @param propertyName The name of the property to evaluate
     * @return a property instance that can be used to build expressions
     */
    Property property(String propertyName);

}
