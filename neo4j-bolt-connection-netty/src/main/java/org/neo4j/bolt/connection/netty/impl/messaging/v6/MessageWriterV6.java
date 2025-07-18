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

import java.util.Map;
import org.neo4j.bolt.connection.netty.impl.messaging.AbstractMessageWriter;
import org.neo4j.bolt.connection.netty.impl.messaging.MessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.BeginMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.CommitMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.DiscardMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.GoodbyeMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.HelloMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.LogoffMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.LogonMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.PullMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.ResetMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.RollbackMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.RouteV44MessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.RunWithMetadataMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.encode.TelemetryMessageEncoder;
import org.neo4j.bolt.connection.netty.impl.messaging.request.BeginMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.CommitMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.DiscardMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.GoodbyeMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.HelloMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.LogoffMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.LogonMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.PullMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.ResetMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.RollbackMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.RouteMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.RunWithMetadataMessage;
import org.neo4j.bolt.connection.netty.impl.messaging.request.TelemetryMessage;
import org.neo4j.bolt.connection.netty.impl.packstream.PackOutput;
import org.neo4j.bolt.connection.values.ValueFactory;

final class MessageWriterV6 extends AbstractMessageWriter {
    public MessageWriterV6(PackOutput output, ValueFactory valueFactory) {
        super(new ValuePackerV6(output), buildEncoders(), valueFactory);
    }

    private static Map<Byte, MessageEncoder> buildEncoders() {
        return Map.ofEntries(
                Map.entry(HelloMessage.SIGNATURE, new HelloMessageEncoder()),
                Map.entry(LogonMessage.SIGNATURE, new LogonMessageEncoder()),
                Map.entry(LogoffMessage.SIGNATURE, new LogoffMessageEncoder()),
                Map.entry(GoodbyeMessage.SIGNATURE, new GoodbyeMessageEncoder()),
                Map.entry(RunWithMetadataMessage.SIGNATURE, new RunWithMetadataMessageEncoder()),
                Map.entry(RouteMessage.SIGNATURE, new RouteV44MessageEncoder()),
                Map.entry(DiscardMessage.SIGNATURE, new DiscardMessageEncoder()),
                Map.entry(PullMessage.SIGNATURE, new PullMessageEncoder()),
                Map.entry(BeginMessage.SIGNATURE, new BeginMessageEncoder()),
                Map.entry(CommitMessage.SIGNATURE, new CommitMessageEncoder()),
                Map.entry(RollbackMessage.SIGNATURE, new RollbackMessageEncoder()),
                Map.entry(ResetMessage.SIGNATURE, new ResetMessageEncoder()),
                Map.entry(TelemetryMessage.SIGNATURE, new TelemetryMessageEncoder()));
    }
}
