package org.sluckframework.cqrs.saga;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;

/**
 * 通用的saga工厂
 *
 * Author: sunxy
 * Created: 2015-09-13 21:37
 * Since: 1.0
 */
public class GenericSagaFactory implements SagaFactory{

    private static final String UNSUITABLE_CTR_MSG = "[%s] is not a suitable type for the GenericSagaFactory. ";
    private ResourceInjector resourceInjector = NullResourceInjector.INSTANCE;

    @Override
    public <T extends Saga> T createSaga(Class<T> sagaType) {
        try {
            T instance = sagaType.getConstructor().newInstance();
            resourceInjector.injectResources(instance);
            return instance;
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(
                    format(UNSUITABLE_CTR_MSG + "It needs an accessible default constructor.",
                            sagaType.getSimpleName()), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                    format(UNSUITABLE_CTR_MSG + "The default constructor is not accessible.",
                            sagaType.getSimpleName()), e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(
                    format(UNSUITABLE_CTR_MSG + "An exception occurred while invoking the default constructor.",
                            sagaType.getSimpleName()), e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                    format(UNSUITABLE_CTR_MSG + "There must be an accessible default (no-arg) constructor.",
                            sagaType.getSimpleName()), e);
        }
    }

    /**
     * 只要是具有无惨的构造函数的都支持
     */
    @Override
    public boolean supports(Class<? extends Saga> sagaType) {
        Constructor<?>[] constructors = sagaType.getConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 配置资源注入器
     *
     * @param resourceInjector The resource injector providing the necessary resources
     */
    public void setResourceInjector(ResourceInjector resourceInjector) {
        this.resourceInjector = resourceInjector;
    }

    private static final class NullResourceInjector implements ResourceInjector {

        public static final NullResourceInjector INSTANCE = new NullResourceInjector();

        private NullResourceInjector() {
        }

        @Override
        public void injectResources(Saga saga) {
        }
    }
}
