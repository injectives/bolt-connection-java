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
package org.neo4j.bolt.connection.netty.impl.util.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;

import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.neo4j.bolt.connection.netty.impl.async.inbound.ByteBufInput;
import org.neo4j.bolt.connection.netty.impl.messaging.Message;
import org.neo4j.bolt.connection.netty.impl.messaging.MessageFormat;
import org.neo4j.bolt.connection.netty.impl.packstream.PackOutput;
import org.neo4j.bolt.connection.netty.impl.packstream.PackStream;
import org.neo4j.bolt.connection.netty.impl.util.io.ByteBufOutput;
import org.neo4j.bolt.connection.test.values.TestValueFactory;
import org.neo4j.bolt.connection.values.ValueFactory;

public abstract class AbstractMessageWriterTestBase {
    protected static final ValueFactory valueFactory = TestValueFactory.INSTANCE;

    @TestFactory
    Stream<DynamicNode> shouldWriteSupportedMessages() {
        return supportedMessages()
                .map(message -> dynamicTest(message.toString(), () -> testSupportedMessageWriting(message)));
    }

    @TestFactory
    Stream<DynamicNode> shouldFailToWriteUnsupportedMessages() {
        return unsupportedMessages()
                .map(message -> dynamicTest(message.toString(), () -> testUnsupportedMessageWriting(message)));
    }

    protected abstract MessageFormat.Writer newWriter(PackOutput output);

    protected abstract Stream<Message> supportedMessages();

    protected abstract Stream<Message> unsupportedMessages();

    private void testSupportedMessageWriting(Message message) throws IOException {
        var buffer = Unpooled.buffer();
        PackOutput output = new ByteBufOutput(buffer);

        var writer = newWriter(output);
        writer.write(message);

        var input = new ByteBufInput();
        input.start(buffer);
        var unpacker = new PackStream.Unpacker(input);

        var structHeader = unpacker.unpackStructHeader();
        assertTrue(structHeader >= 0L);

        var structSignature = unpacker.unpackStructSignature();
        assertEquals(message.signature(), structSignature);
    }

    private void testUnsupportedMessageWriting(Message message) {
        var writer = newWriter(mock(PackOutput.class));
        assertThrows(Exception.class, () -> writer.write(message));
    }
}
