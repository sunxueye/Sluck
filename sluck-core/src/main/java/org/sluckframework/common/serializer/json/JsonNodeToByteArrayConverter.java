package org.sluckframework.common.serializer.json;


import org.sluckframework.common.serializer.AbstractContentTypeConverter;
import org.sluckframework.common.serializer.CannotConvertBetweenTypesException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JsonNode to byte[] 转换
 * 
 * @author sunxy
 * @time 2015年8月30日 上午2:52:36
 * @since 1.0
 */
public class JsonNodeToByteArrayConverter extends AbstractContentTypeConverter<JsonNode, byte[]> {

    private final ObjectMapper objectMapper;

    public JsonNodeToByteArrayConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Class<JsonNode> expectedSourceType() {
        return JsonNode.class;
    }

    @Override
    public Class<byte[]> targetType() {
        return byte[].class;
    }

    @Override
    public byte[] convert(JsonNode original) {
        try {
            return objectMapper.writeValueAsBytes(original);
        } catch (JsonProcessingException e) {
            throw new CannotConvertBetweenTypesException("An error occurred while converting a JsonNode to byte[]", e);
        }
    }	
	

}
