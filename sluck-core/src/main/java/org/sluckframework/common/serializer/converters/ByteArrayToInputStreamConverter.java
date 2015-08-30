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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.sluckframework.common.serializer.AbstractContentTypeConverter;

/**
 * byte[] 与 ByteArrayInputStream的转换
 *
 * @author sunxy
 * @since 1.0
 */
public class ByteArrayToInputStreamConverter extends AbstractContentTypeConverter<byte[], InputStream> {

    @Override
    public Class<byte[]> expectedSourceType() {
        return byte[].class;
    }

    @Override
    public Class<InputStream> targetType() {
        return InputStream.class;
    }

    @Override
    public InputStream convert(byte[] original) {
        return new ByteArrayInputStream(original);
    }
}
