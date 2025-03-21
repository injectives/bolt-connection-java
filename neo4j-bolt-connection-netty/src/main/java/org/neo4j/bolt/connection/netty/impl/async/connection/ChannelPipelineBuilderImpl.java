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
package org.neo4j.bolt.connection.netty.impl.async.connection;

import static org.neo4j.bolt.connection.netty.impl.async.connection.ChannelAttributes.addBoltPatchesListener;

import io.netty.channel.ChannelPipeline;
import org.neo4j.bolt.connection.LoggingProvider;
import org.neo4j.bolt.connection.netty.impl.async.inbound.ChannelErrorHandler;
import org.neo4j.bolt.connection.netty.impl.async.inbound.ChunkDecoder;
import org.neo4j.bolt.connection.netty.impl.async.inbound.InboundMessageHandler;
import org.neo4j.bolt.connection.netty.impl.async.inbound.MessageDecoder;
import org.neo4j.bolt.connection.netty.impl.async.outbound.OutboundMessageHandler;
import org.neo4j.bolt.connection.netty.impl.messaging.MessageFormat;
import org.neo4j.bolt.connection.values.ValueFactory;

public class ChannelPipelineBuilderImpl implements ChannelPipelineBuilder {
    @Override
    public void build(
            MessageFormat messageFormat, ChannelPipeline pipeline, LoggingProvider logging, ValueFactory valueFactory) {
        // inbound handlers
        pipeline.addLast(new ChunkDecoder(logging));
        pipeline.addLast(new MessageDecoder());
        var channel = pipeline.channel();
        var inboundMessageHandler = new InboundMessageHandler(messageFormat, logging, valueFactory);
        addBoltPatchesListener(channel, inboundMessageHandler);
        pipeline.addLast(inboundMessageHandler);

        // outbound handlers
        var outboundMessageHandler = new OutboundMessageHandler(messageFormat, logging, valueFactory);
        addBoltPatchesListener(channel, outboundMessageHandler);
        pipeline.addLast(OutboundMessageHandler.NAME, outboundMessageHandler);

        // last one - error handler
        pipeline.addLast(new ChannelErrorHandler(logging));
    }
}
