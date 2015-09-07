package org.sluckframework.cqrs.commandhandling.callback;

import org.sluckframework.cqrs.commandhandling.CommandCallback;

/**
 * 无回调
 * 
 * @author sunxy
 * @time 2015年9月7日 下午2:01:54	
 * @since 1.0
 */
public class NoOpCallback implements CommandCallback<Object> {

    public static final NoOpCallback INSTANCE = new NoOpCallback();

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation does nothing.
     */
    @Override
    public void onSuccess(Object result) {
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This implementation does nothing.
     */
    @Override
    public void onFailure(Throwable cause) {
    }
}
