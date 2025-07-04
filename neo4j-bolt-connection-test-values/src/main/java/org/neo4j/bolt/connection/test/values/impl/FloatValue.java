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

public class FloatValue extends NumberValueAdapter<Double> {
    private final double val;

    public FloatValue(double val) {
        this.val = val;
    }

    @Override
    public Type boltValueType() {
        return Type.FLOAT;
    }

    @Override
    public Double asNumber() {
        return val;
    }

    @Override
    public long asLong() {
        var longVal = (long) val;
        if ((double) longVal != val) {
            throw new LossyCoercion(boltValueType().name(), "Java long");
        }

        return longVal;
    }

    @Override
    public double asDouble() {
        return val;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var values = (FloatValue) o;
        return Double.compare(values.val, val) == 0;
    }

    @Override
    public int hashCode() {
        var temp = Double.doubleToLongBits(val);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return Double.toString(val);
    }
}
