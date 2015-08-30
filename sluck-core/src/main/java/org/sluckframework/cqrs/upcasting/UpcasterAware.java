package org.sluckframework.cqrs.upcasting;

/**
 * 实现此接口表明可以 使用 upcasting 机制 来转换事件
 * 
 * @author sunxy
 * @time 2015年8月29日 下午5:46:00
 * @since 1.0
 */
public interface UpcasterAware {
	
	 /**
     * 设置转换链  来转换 序列化的事件信息
     *
     * @param upcasterChain 
     */
    void setUpcasterChain(UpcasterChain upcasterChain);

}
