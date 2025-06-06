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
package org.neo4j.bolt.connection.netty.impl.messaging.request;

import static org.neo4j.bolt.connection.netty.impl.util.MetadataExtractor.ABSENT_QUERY_ID;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.neo4j.bolt.connection.netty.impl.messaging.Message;
import org.neo4j.bolt.connection.values.Value;
import org.neo4j.bolt.connection.values.ValueFactory;

public abstract class AbstractStreamingMessage implements Message {
    private final Map<String, Value> metadata = new HashMap<>();

    AbstractStreamingMessage(long n, long id, ValueFactory valueFactory) {
        this.metadata.put("n", valueFactory.value(n));
        if (id != ABSENT_QUERY_ID) {
            this.metadata.put("qid", valueFactory.value(id));
        }
    }

    public Map<String, Value> metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (AbstractStreamingMessage) o;
        return Objects.equals(metadata, that.metadata);
    }

    protected abstract String name();

    @Override
    public int hashCode() {
        return Objects.hash(metadata);
    }

    @Override
    public String toString() {
        return String.format("%s %s", name(), metadata);
    }
}
