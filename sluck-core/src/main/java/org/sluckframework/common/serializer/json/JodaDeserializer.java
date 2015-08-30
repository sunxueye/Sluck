package org.sluckframework.common.serializer.json;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.sluckframework.common.exception.SluckConfigurationException;
import org.sluckframework.common.serializer.SerializationException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * 自定义 joda 解析器 
 * 
 * @author sunxy
 * @time 2015年8月30日 上午2:38:44
 * @since 1.0
 */
public class JodaDeserializer<T> extends JsonDeserializer<T> {

    private final Constructor<T> constructor;

    /**
     * 使用给定的 class 初始化
     * @param instantType The type of object to serialize into
     */
    public JodaDeserializer(Class<T> instantType) {
        try {
            this.constructor = instantType.getConstructor(Object.class);
        } catch (NoSuchMethodException e) {
            throw new SluckConfigurationException(
                    "The type " + instantType.getName() + " isn't compatible with the JodaDeserializer", e);
        }
    }

    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        try {
            return constructor.newInstance(jp.readValueAs(String.class));
        } catch (Exception e) {
            throw new SerializationException("Unable to read instant from JSON document", e);
        }
    }	
	

}
