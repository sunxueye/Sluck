package org.sluckframework.cqrs.commandhandling.callback;

import org.sluckframework.cqrs.commandhandling.CommandCallback;

/**
 * 不需要返回 参数的 回调
 * 
 * @author sunxy
 * @time 2015年9月7日 下午2:05:10	
 * @since 1.0
 */
public abstract class VoidCallback implements CommandCallback<Object> {

   /**
     * {@inheritDoc}
     */
    @Override
    public void onSuccess(Object result) {
        onSuccess();
    }

    protected abstract void onSuccess();

}
