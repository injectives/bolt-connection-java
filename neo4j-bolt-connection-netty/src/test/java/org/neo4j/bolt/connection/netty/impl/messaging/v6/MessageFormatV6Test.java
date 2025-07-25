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
package org.neo4j.bolt.connection.netty.impl.messaging.v6;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.neo4j.bolt.connection.netty.impl.messaging.MessageFormat;
import org.neo4j.bolt.connection.netty.impl.packstream.PackInput;
import org.neo4j.bolt.connection.netty.impl.packstream.PackOutput;
import org.neo4j.bolt.connection.values.ValueFactory;

class MessageFormatV6Test {
    private static final MessageFormat format = BoltProtocolV6.INSTANCE.createMessageFormat();

    @Test
    void shouldCreateCorrectWriter() {
        var writer = format.newWriter(mock(PackOutput.class), mock(ValueFactory.class));

        assertInstanceOf(MessageWriterV6.class, writer);
    }

    @Test
    void shouldCreateCorrectReader() {
        var reader = format.newReader(mock(PackInput.class), mock(ValueFactory.class));

        assertInstanceOf(MessageReaderV6.class, reader);
    }
}
