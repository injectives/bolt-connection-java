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
package org.neo4j.bolt.connection.netty.impl.spi;

import io.netty.channel.EventLoop;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import org.neo4j.bolt.connection.BoltServerAddress;
import org.neo4j.bolt.connection.netty.impl.messaging.BoltProtocol;
import org.neo4j.bolt.connection.netty.impl.messaging.Message;

public interface Connection {
    boolean isOpen();

    void enableAutoRead();

    void disableAutoRead();

    CompletionStage<Void> write(Message message, ResponseHandler handler);

    CompletionStage<Void> flush();

    boolean isTelemetryEnabled();

    boolean isSsrEnabled();

    String serverAgent();

    BoltServerAddress serverAddress();

    BoltProtocol protocol();

    CompletionStage<Void> forceClose(String reason);

    CompletionStage<Void> close();

    EventLoop eventLoop();

    Optional<Duration> defaultReadTimeoutMillis();

    void setReadTimeout(Duration duration);
}
