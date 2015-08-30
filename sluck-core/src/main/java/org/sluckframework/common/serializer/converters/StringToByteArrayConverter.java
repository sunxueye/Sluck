/*
 * Copyright (c) 2010-2014. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sluckframework.common.serializer.converters;

import java.nio.charset.Charset;

import org.sluckframework.common.serializer.AbstractContentTypeConverter;

/**
 * String 与 btye[]的转换
 *
 * @author sunxy
 * @since 1.0
 */
public class StringToByteArrayConverter extends AbstractContentTypeConverter<String, byte[]> {

    private static final Charset UTF8 = Charset.forName("UTF-8");

    @Override
    public Class<String> expectedSourceType() {
        return String.class;
    }

    @Override
    public Class<byte[]> targetType() {
        return byte[].class;
    }

    @Override
    public byte[] convert(String original) {
        return original.getBytes(UTF8);
    }
}
