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

import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.neo4j.bolt.connection.GqlError;
import org.neo4j.bolt.connection.LoggingProvider;
import org.neo4j.bolt.connection.netty.impl.async.inbound.InboundMessageDispatcher;
import org.neo4j.bolt.connection.netty.impl.messaging.Message;
import org.neo4j.bolt.connection.netty.impl.messaging.response.FailureMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.response.IgnoredMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.response.RecordMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.response.SuccessMessage;
import org.neo4j.bolt.connection.values.Value;

public class MemorizingInboundMessageDispatcher extends InboundMessageDispatcher {
    private final List<Message> messages = new CopyOnWriteArrayList<>();

    public MemorizingInboundMessageDispatcher(Channel channel, LoggingProvider logging) {
        super(channel, logging);
    }

    public List<Message> messages() {
        return new ArrayList<>(messages);
    }

    @Override
    public void handleSuccessMessage(Map<String, Value> meta) {
        messages.add(new SuccessMessage(meta));
    }

    @Override
    public void handleRecordMessage(List<Value> fields) {
        messages.add(new RecordMessage(fields));
    }

    @Override
    public void handleFailureMessage(GqlError gqlError) {
        messages.add(new FailureMessage(gqlError.code(), gqlError.message()));
    }

    @Override
    public void handleIgnoredMessage() {
        messages.add(IgnoredMessage.IGNORED);
    }
}
