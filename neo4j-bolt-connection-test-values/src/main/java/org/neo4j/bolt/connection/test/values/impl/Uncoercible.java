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

import static java.lang.String.format;

import java.io.Serial;

public class Uncoercible extends ValueException {
    @Serial
    private static final long serialVersionUID = -6259981390929065201L;

    public Uncoercible(String sourceTypeName, String destinationTypeName) {
        super(format("Cannot coerce %s to %s", sourceTypeName, destinationTypeName));
    }
}
