/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [https://neo4j.com]
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
package org.neo4j.bolt.connection.netty.impl.messaging.v43;

import org.neo4j.bolt.connection.netty.impl.messaging.MessageFormat;
import org.neo4j.bolt.connection.netty.impl.messaging.common.CommonMessageReader;
import org.neo4j.bolt.connection.netty.impl.packstream.PackInput;
import org.neo4j.bolt.connection.netty.impl.packstream.PackOutput;
import org.neo4j.bolt.connection.values.ValueFactory;

/**
 * Bolt message format v4.3
 */
public class MessageFormatV43 implements MessageFormat {
    private boolean dateTimeUtcEnabled;

    @Override
    public Writer newWriter(PackOutput output, ValueFactory valueFactory) {
        return new MessageWriterV43(output, dateTimeUtcEnabled, valueFactory);
    }

    @Override
    public Reader newReader(PackInput input, ValueFactory valueFactory) {
        return new CommonMessageReader(input, dateTimeUtcEnabled, valueFactory);
    }

    @Override
    public void enableDateTimeUtc() {
        dateTimeUtcEnabled = true;
    }
}
