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
package org.neo4j.bolt.connection.netty.impl.messaging.encode;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.neo4j.bolt.connection.netty.impl.messaging.ValuePacker;
import org.neo4j.bolt.connection.netty.impl.messaging.request.DiscardAllMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.DiscardMessage;
import org.neo4j.bolt.connection.test.values.TestValueFactory;
import org.neo4j.bolt.connection.values.Value;
import org.neo4j.bolt.connection.values.ValueFactory;

class DiscardMessageEncoderTest {
    private static final ValueFactory valueFactory = TestValueFactory.INSTANCE;
    private final DiscardMessageEncoder encoder = new DiscardMessageEncoder();
    private final ValuePacker packer = mock(ValuePacker.class);

    @Test
    void shouldDiscardAllCorrectly() throws Throwable {
        encoder.encode(new DiscardMessage(-1, -1, valueFactory), packer, valueFactory);

        Map<String, Value> meta = new HashMap<>();
        meta.put("n", valueFactory.value(-1));

        var order = inOrder(packer);
        order.verify(packer).packStructHeader(1, DiscardMessage.SIGNATURE);
        order.verify(packer).pack(meta);
    }

    @Test
    void shouldEncodeDiscardMessage() throws Exception {
        encoder.encode(new DiscardMessage(100, 200, valueFactory), packer, valueFactory);

        Map<String, Value> meta = new HashMap<>();
        meta.put("n", valueFactory.value(100));
        meta.put("qid", valueFactory.value(200));

        var order = inOrder(packer);
        order.verify(packer).packStructHeader(1, DiscardMessage.SIGNATURE);
        order.verify(packer).pack(meta);
    }

    @Test
    void shouldAvoidQueryId() throws Throwable {
        encoder.encode(new DiscardMessage(100, -1, valueFactory), packer, valueFactory);

        Map<String, Value> meta = new HashMap<>();
        meta.put("n", valueFactory.value(100));

        var order = inOrder(packer);
        order.verify(packer).packStructHeader(1, DiscardMessage.SIGNATURE);
        order.verify(packer).pack(meta);
    }

    @Test
    void shouldFailToEncodeWrongMessage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> encoder.encode(DiscardAllMessage.DISCARD_ALL, packer, valueFactory));
    }
}
