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
package org.neo4j.bolt.connection.netty.impl.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.neo4j.bolt.connection.netty.impl.util.MetadataExtractor.extractServer;

import org.junit.jupiter.api.Test;
import org.neo4j.bolt.connection.exception.BoltUntrustedServerException;
import org.neo4j.bolt.connection.test.values.TestValueFactory;
import org.neo4j.bolt.connection.values.ValueFactory;

class MetadataExtractorTest {
    private static final ValueFactory valueFactory = TestValueFactory.INSTANCE;
    private static final String RESULT_AVAILABLE_AFTER_KEY = "available_after";

    private final MetadataExtractor extractor = new MetadataExtractor(RESULT_AVAILABLE_AFTER_KEY);

    @Test
    void shouldExtractQueryKeys() {
        var keys = asList("hello", " ", "world", "!");

        var extracted = extractor.extractQueryKeys(singletonMap("fields", valueFactory.value(keys)));
        assertEquals(keys, extracted);
    }

    @Test
    void shouldExtractEmptyQueryKeysWhenNoneInMetadata() {
        var extracted = extractor.extractQueryKeys(emptyMap());
        assertEquals(emptyList(), extracted);
    }

    @Test
    void shouldExtractResultAvailableAfter() {
        var metadata = singletonMap(RESULT_AVAILABLE_AFTER_KEY, valueFactory.value(424242));
        var extractedResultAvailableAfter = extractor.extractResultAvailableAfter(metadata);
        assertEquals(424242L, extractedResultAvailableAfter);
    }

    @Test
    void shouldExtractNoResultAvailableAfterWhenNoneInMetadata() {
        var extractedResultAvailableAfter = extractor.extractResultAvailableAfter(emptyMap());
        assertEquals(-1, extractedResultAvailableAfter);
    }

    @Test
    void shouldExtractServer() {
        var agent = "Neo4j/3.5.0";
        var metadata = singletonMap("server", valueFactory.value(agent));

        var serverValue = extractServer(metadata);

        assertEquals(agent, serverValue.asString());
    }

    @Test
    void shouldFailToExtractServerVersionWhenMetadataDoesNotContainIt() {
        assertThrows(
                BoltUntrustedServerException.class,
                () -> extractServer(singletonMap("server", valueFactory.value((Object) null))));
        assertThrows(BoltUntrustedServerException.class, () -> extractServer(singletonMap("server", null)));
    }

    @Test
    void shouldFailToExtractServerVersionFromNonNeo4jProduct() {
        assertThrows(
                BoltUntrustedServerException.class,
                () -> extractServer(singletonMap("server", valueFactory.value("NotNeo4j/1.2.3"))));
    }
}
