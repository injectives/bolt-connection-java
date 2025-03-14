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
package org.neo4j.bolt.connection.test.values.impl;

import java.util.Map;
import java.util.function.Function;
import org.neo4j.bolt.connection.values.Value;

public abstract class InternalEntity implements Entity, AsValue {
    private final long id;
    private final String elementId;
    private final Map<String, Value> properties;

    public InternalEntity(long id, String elementId, Map<String, Value> properties) {
        this.id = id;
        this.elementId = elementId;
        this.properties = properties;
    }

    @Override
    @Deprecated
    public long id() {
        return id;
    }

    @Override
    public String elementId() {
        return elementId;
    }

    @Override
    public int size() {
        return properties.size();
    }

    @Override
    public <T> Map<String, T> asMap(Function<Value, T> mapFunction) {
        return Extract.map(properties, mapFunction);
    }

    @Override
    public Iterable<String> keys() {
        return properties.keySet();
    }

    @Override
    public Value get(String key) {
        var value = properties.get(key);
        return value == null ? NullValue.NULL : value;
    }

    @Override
    public Value asValue() {
        return new MapValue(properties);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var that = (InternalEntity) o;

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Entity{" + "id=" + id + ", properties=" + properties + '}';
    }
}
