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

import java.util.Arrays;
import org.neo4j.bolt.connection.values.Type;

public class BytesValue extends ValueAdapter {
    private final byte[] val;

    public BytesValue(byte[] val) {
        if (val == null) {
            throw new IllegalArgumentException("Cannot construct BytesValue from null");
        }
        this.val = val;
    }

    @Override
    public boolean isEmpty() {
        return val.length == 0;
    }

    @Override
    public int size() {
        return val.length;
    }

    @Override
    public byte[] asByteArray() {
        return val;
    }

    @Override
    public Type boltValueType() {
        return Type.BYTES;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        var values = (BytesValue) o;
        return Arrays.equals(val, values.val);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(val);
    }

    @Override
    public String toString() {
        var s = new StringBuilder("#");
        for (var b : val) {
            if (b < 0x10) {
                s.append('0');
            }
            s.append(Integer.toHexString(b));
        }
        return s.toString();
    }
}
