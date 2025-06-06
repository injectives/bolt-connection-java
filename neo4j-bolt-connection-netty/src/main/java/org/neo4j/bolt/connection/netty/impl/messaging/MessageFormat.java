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
package org.neo4j.bolt.connection.netty.impl.messaging;

import java.io.IOException;
import org.neo4j.bolt.connection.netty.impl.packstream.PackInput;
import org.neo4j.bolt.connection.netty.impl.packstream.PackOutput;
import org.neo4j.bolt.connection.values.ValueFactory;

public interface MessageFormat {
    interface Writer {
        void write(Message msg) throws IOException;
    }

    interface Reader {
        void read(ResponseMessageHandler handler) throws IOException;
    }

    Writer newWriter(PackOutput output, ValueFactory valueFactory);

    Reader newReader(PackInput input, ValueFactory valueFactory);

    /**
     * Enables datetime in UTC if supported by the given message format. This is only for use with formats that support multiple modes.
     * <p>
     * This only takes effect on subsequent writer and reader creation via {@link #newWriter(PackOutput, ValueFactory)} and {@link #newReader(PackInput, ValueFactory)}.
     */
    default void enableDateTimeUtc() {}
}
