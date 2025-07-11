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
package org.neo4j.bolt.connection.netty.impl.messaging.v4;

import java.time.Clock;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.neo4j.bolt.connection.AccessMode;
import org.neo4j.bolt.connection.BoltProtocolVersion;
import org.neo4j.bolt.connection.BoltServerAddress;
import org.neo4j.bolt.connection.ClusterComposition;
import org.neo4j.bolt.connection.DatabaseName;
import org.neo4j.bolt.connection.LoggingProvider;
import org.neo4j.bolt.connection.NotificationConfig;
import org.neo4j.bolt.connection.netty.impl.handlers.PullResponseHandlerImpl;
import org.neo4j.bolt.connection.netty.impl.handlers.RunResponseHandler;
import org.neo4j.bolt.connection.netty.impl.messaging.BoltProtocol;
import org.neo4j.bolt.connection.netty.impl.messaging.MessageFormat;
import org.neo4j.bolt.connection.netty.impl.messaging.MessageHandler;
import org.neo4j.bolt.connection.netty.impl.messaging.PullMessageHandler;
import org.neo4j.bolt.connection.netty.impl.messaging.request.PullMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.RunWithMetadataMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.v3.BoltProtocolV3;
import org.neo4j.bolt.connection.netty.impl.spi.Connection;
import org.neo4j.bolt.connection.summary.PullSummary;
import org.neo4j.bolt.connection.summary.RouteSummary;
import org.neo4j.bolt.connection.summary.RunSummary;
import org.neo4j.bolt.connection.values.Value;
import org.neo4j.bolt.connection.values.ValueFactory;

public class BoltProtocolV4 extends BoltProtocolV3 {
    public static final BoltProtocolVersion VERSION = new BoltProtocolVersion(4, 0);
    public static final BoltProtocol INSTANCE = new BoltProtocolV4();
    private static final String ROUTING_CONTEXT = "context";
    private static final String DATABASE_NAME = "database";
    private static final String MULTI_DB_GET_ROUTING_TABLE =
            String.format("CALL dbms.routing.getRoutingTable($%s, $%s)", ROUTING_CONTEXT, DATABASE_NAME);

    @Override
    public MessageFormat createMessageFormat() {
        return new MessageFormatV4();
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public CompletionStage<Void> route(
            Connection connection,
            Map<String, Value> routingContext,
            Set<String> bookmarks,
            String databaseName,
            String impersonatedUser,
            MessageHandler<RouteSummary> handler,
            Clock clock,
            LoggingProvider logging,
            ValueFactory valueFactory) {
        var query = new Query(
                MULTI_DB_GET_ROUTING_TABLE,
                Map.of(
                        ROUTING_CONTEXT,
                        valueFactory.value(routingContext),
                        DATABASE_NAME,
                        valueFactory.value(databaseName)));

        var runMessage = RunWithMetadataMessage.autoCommitTxRunMessage(
                query.query(),
                query.parameters(),
                null,
                Collections.emptyMap(),
                DatabaseName.systemDatabase(),
                AccessMode.READ,
                bookmarks,
                null,
                NotificationConfig.defaultConfig(),
                useLegacyNotifications(),
                logging,
                valueFactory);
        var runFuture = new CompletableFuture<RunSummary>();
        var runHandler = new RunResponseHandler(runFuture, METADATA_EXTRACTOR);
        var pullFuture = new CompletableFuture<Map<String, Value>>();

        runFuture
                .thenCompose(ignored -> pullFuture)
                .thenApply(map -> {
                    var ttl = map.get("ttl").asLong();
                    var expirationTimestamp = clock.millis() + ttl * 1000;
                    if (ttl < 0 || ttl >= Long.MAX_VALUE / 1000L || expirationTimestamp < 0) {
                        expirationTimestamp = Long.MAX_VALUE;
                    }

                    Set<BoltServerAddress> readers = new LinkedHashSet<>();
                    Set<BoltServerAddress> writers = new LinkedHashSet<>();
                    Set<BoltServerAddress> routers = new LinkedHashSet<>();

                    for (var serversMap : map.get("servers").boltValues()) {
                        var role = serversMap.getBoltValue("role").asString();
                        for (var server : serversMap.getBoltValue("addresses").boltValues()) {
                            var address = new BoltServerAddress(server.asString());
                            switch (role) {
                                case "WRITE" -> writers.add(address);
                                case "READ" -> readers.add(address);
                                case "ROUTE" -> routers.add(address);
                            }
                        }
                    }
                    var db = map.get("db");
                    String name = null;
                    if (db != null && !db.isNull()) {
                        name = db.asString();
                    }

                    var clusterComposition =
                            new ClusterComposition(expirationTimestamp, readers, writers, routers, name);
                    return new RouteSummaryImpl(clusterComposition);
                })
                .whenComplete((summary, throwable) -> {
                    if (throwable != null) {
                        handler.onError(throwable);
                    } else {
                        handler.onSummary(summary);
                    }
                });

        return connection.write(runMessage, runHandler).thenCompose(ignored -> {
            var pullMessage = new PullMessage(-1, -1, valueFactory);
            var pullHandler = new PullResponseHandlerImpl(
                    new PullMessageHandler() {
                        private Map<String, Value> routingTable;

                        @Override
                        public void onRecord(List<Value> fields) {
                            if (routingTable == null) {
                                var keys = runFuture.join().keys();
                                routingTable = new HashMap<>(keys.size());
                                for (var i = 0; i < keys.size(); i++) {
                                    routingTable.put(keys.get(i), fields.get(i));
                                }
                                routingTable = Collections.unmodifiableMap(routingTable);
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            pullFuture.completeExceptionally(throwable);
                        }

                        @Override
                        public void onSummary(PullSummary success) {
                            pullFuture.complete(routingTable);
                        }
                    },
                    valueFactory);
            return connection.write(pullMessage, pullHandler);
        });
    }

    @Override
    public CompletionStage<Void> pull(
            Connection connection, long qid, long request, PullMessageHandler handler, ValueFactory valueFactory) {
        var pullMessage = new PullMessage(request, qid, valueFactory);
        var pullHandler = new PullResponseHandlerImpl(handler, valueFactory);
        return connection.write(pullMessage, pullHandler);
    }

    @Override
    protected void verifyDatabaseNameBeforeTransaction(DatabaseName databaseName) {
        // Bolt V4 accepts database name
    }

    @Override
    public BoltProtocolVersion version() {
        return VERSION;
    }

    private record RouteSummaryImpl(ClusterComposition clusterComposition) implements RouteSummary {}
}
