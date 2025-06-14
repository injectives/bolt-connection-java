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

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.neo4j.bolt.connection.AccessMode.READ;
import static org.neo4j.bolt.connection.DatabaseName.defaultDatabase;
import static org.neo4j.bolt.connection.netty.impl.messaging.request.DiscardAllMessage.DISCARD_ALL;
import static org.neo4j.bolt.connection.netty.impl.messaging.request.RunWithMetadataMessage.autoCommitTxRunMessage;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.neo4j.bolt.connection.AccessMode;
import org.neo4j.bolt.connection.LoggingProvider;
import org.neo4j.bolt.connection.netty.impl.messaging.ValuePacker;
import org.neo4j.bolt.connection.netty.impl.messaging.request.RunWithMetadataMessage;
import org.neo4j.bolt.connection.test.values.TestValueFactory;
import org.neo4j.bolt.connection.values.Value;
import org.neo4j.bolt.connection.values.ValueFactory;

class RunWithMetadataMessageEncoderTest {
    private static final ValueFactory valueFactory = TestValueFactory.INSTANCE;
    private final RunWithMetadataMessageEncoder encoder = new RunWithMetadataMessageEncoder();
    private final ValuePacker packer = mock(ValuePacker.class);

    @ParameterizedTest
    @EnumSource(AccessMode.class)
    void shouldEncodeRunWithMetadataMessage(AccessMode mode) throws Exception {
        var params = singletonMap("answer", valueFactory.value(42));

        var bookmarks = Collections.singleton("neo4j:bookmark:v1:tx999");

        Map<String, Value> txMetadata = new HashMap<>();
        txMetadata.put("key1", valueFactory.value("value1"));
        txMetadata.put("key2", valueFactory.value(List.of(1, 2, 3, 4, 5)));
        txMetadata.put("key3", valueFactory.value(true));

        var txTimeout = Duration.ofMillis(42);

        encoder.encode(
                autoCommitTxRunMessage(
                        "RETURN $answer",
                        params,
                        txTimeout,
                        txMetadata,
                        defaultDatabase(),
                        mode,
                        bookmarks,
                        null,
                        null,
                        false,
                        new LoggingProvider() {
                            @Override
                            public System.Logger getLog(Class<?> cls) {
                                return mock(System.Logger.class);
                            }

                            @Override
                            public System.Logger getLog(String name) {
                                return mock(System.Logger.class);
                            }
                        },
                        valueFactory),
                packer,
                valueFactory);

        var order = inOrder(packer);
        order.verify(packer).packStructHeader(3, RunWithMetadataMessage.SIGNATURE);
        order.verify(packer).pack("RETURN $answer");
        order.verify(packer).pack(params);

        Map<String, Value> expectedMetadata = new HashMap<>();
        expectedMetadata.put(
                "bookmarks",
                valueFactory.value(bookmarks.stream().map(valueFactory::value).collect(Collectors.toSet())));
        expectedMetadata.put("tx_timeout", valueFactory.value(42));
        expectedMetadata.put("tx_metadata", valueFactory.value(txMetadata));
        if (mode == READ) {
            expectedMetadata.put("mode", valueFactory.value("r"));
        }

        order.verify(packer).pack(expectedMetadata);
    }

    @Test
    void shouldFailToEncodeWrongMessage() {
        assertThrows(IllegalArgumentException.class, () -> encoder.encode(DISCARD_ALL, packer, valueFactory));
    }
}
