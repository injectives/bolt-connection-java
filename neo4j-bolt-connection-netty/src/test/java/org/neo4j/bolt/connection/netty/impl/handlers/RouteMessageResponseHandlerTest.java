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
package org.neo4j.bolt.connection.netty.impl.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;
import org.neo4j.bolt.connection.test.values.TestValueFactory;
import org.neo4j.bolt.connection.values.Value;
import org.neo4j.bolt.connection.values.ValueFactory;

class RouteMessageResponseHandlerTest {
    private static final ValueFactory valueFactory = TestValueFactory.INSTANCE;

    @Test
    void onSuccessShouldSuccessFullyCompleteFutureWithRoutingTable() {
        var completableFuture = new CompletableFuture<Map<String, Value>>();
        var responseHandler = new RouteMessageResponseHandler(completableFuture, valueFactory);
        var routingTable = getRoutingTable();
        var metadata = getMetadataWithRoutingTable(routingTable);

        responseHandler.onSuccess(metadata);

        assertEquals(routingTable, completableFuture.getNow(null));
    }

    @Test
    void onSuccessShouldExceptionallyCompleteFutureWhenMetadataDoesNotHaveRoutingTable() {
        var completableFuture = new CompletableFuture<Map<String, Value>>();
        var responseHandler = new RouteMessageResponseHandler(completableFuture, valueFactory);
        Map<String, Value> metadata = new HashMap<>();

        responseHandler.onSuccess(metadata);

        assertThrows(CompletionException.class, () -> completableFuture.getNow(null));
    }

    @Test
    void onFailureShouldCompleteExceptionallyWithTheOriginalException() {
        var completableFuture = new CompletableFuture<Map<String, Value>>();
        var responseHandler = new RouteMessageResponseHandler(completableFuture, valueFactory);
        var expectedException = new RuntimeException("Test exception");

        responseHandler.onFailure(expectedException);

        assertTrue(completableFuture.isCompletedExceptionally());
        completableFuture.handle((value, ex) -> {
            assertNull(value);
            assertEquals(expectedException, ex);
            return null;
        });
    }

    @Test
    void onRecordShouldThrowUnsupportedOperation() {
        var completableFuture = new CompletableFuture<Map<String, Value>>();
        var responseHandler = new RouteMessageResponseHandler(completableFuture, valueFactory);

        responseHandler.onRecord(List.of());

        assertTrue(completableFuture.isCompletedExceptionally());
        completableFuture.handle((value, ex) -> {
            assertNull(value);
            assertEquals(UnsupportedOperationException.class, ex.getClass());
            return null;
        });
    }

    private Map<String, Value> getMetadataWithRoutingTable(Map<String, Value> routingTable) {
        Map<String, Value> metadata = new HashMap<>();
        metadata.put("rt", valueFactory.value(routingTable));
        return metadata;
    }

    private Map<String, Value> getRoutingTable() {
        Map<String, Value> routingTable = new HashMap<>();
        routingTable.put("ttl", valueFactory.value(300));
        routingTable.put("addresses", valueFactory.value(new ArrayList<>()));
        return routingTable;
    }
}
