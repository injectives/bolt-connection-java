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

import org.neo4j.bolt.connection.values.Type;

public class IntegerValue extends NumberValueAdapter<Long> {
    private final long val;

    public IntegerValue(long val) {
        this.val = val;
    }

    @Override
    public Type boltValueType() {
        return Type.INTEGER;
    }

    @Override
    public Long asNumber() {
        return val;
    }

    @Override
    public long asLong() {
        return val;
    }

    @Override
    public double asDouble() {
        var doubleVal = (double) val;
        if ((long) doubleVal != val) {
            throw new LossyCoercion(boltValueType().name(), "Java double");
        }

        return (double) val;
    }

    @Override
    public String toString() {
        return Long.toString(val);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var values = (IntegerValue) o;
        return val == values.val;
    }

    @Override
    public int hashCode() {
        return (int) (val ^ (val >>> 32));
    }
}
