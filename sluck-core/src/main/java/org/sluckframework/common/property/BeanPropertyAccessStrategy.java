package org.sluckframework.common.property;

import static java.lang.String.format;
import static java.util.Locale.ENGLISH;

/**
 * JavaBean形式的参数获取策略,如 name -> getName
 *
 * Author: sunxy
 * Created: 2015-09-10 23:48
 * Since: 1.0
 */
public class BeanPropertyAccessStrategy extends AbstractMethodPropertyAccessStrategy {

    @Override
    protected String getterName(String property) {
        return format(ENGLISH, "get%S%s", property.charAt(0), property.substring(1));
    }

    @Override
    protected int getPriority() {
        return 0;
    }
}

