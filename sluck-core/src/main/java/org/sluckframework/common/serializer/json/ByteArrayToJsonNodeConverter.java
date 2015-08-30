package org.sluckframework.common.serializer.json;


import java.io.IOException;




import org.sluckframework.common.serializer.AbstractContentTypeConverter;
import org.sluckframework.common.serializer.CannotConvertBetweenTypesException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * byte[] to JsonNode 的转换
 * 
 * @author sunxy
 * @time 2015年8月30日 上午2:54:16
 * @since 1.0
 */
public class ByteArrayToJsonNodeConverter extends AbstractContentTypeConverter<byte[], JsonNode> {

    private final ObjectMapper objectMapper;

    public ByteArrayToJsonNodeConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Class<byte[]> expectedSourceType() {
        return byte[].class;
    }

    @Override
    public Class<JsonNode> targetType() {
        return JsonNode.class;
    }

    @Override
    public JsonNode convert(byte[] original) {
        try {
            return objectMapper.readTree(original);
        } catch (IOException e) {
            throw new CannotConvertBetweenTypesException("An error occurred while converting a JsonNode to byte[]", e);
        }
    }

}
