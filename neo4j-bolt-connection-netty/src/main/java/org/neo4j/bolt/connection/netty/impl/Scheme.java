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
package org.neo4j.bolt.connection.netty.impl;

import java.util.List;

class Scheme {
    public static final String NEO4J_URI_SCHEME = "neo4j";
    public static final String NEO4J_HIGH_TRUST_URI_SCHEME = "neo4j+s";
    public static final String NEO4J_LOW_TRUST_URI_SCHEME = "neo4j+ssc";

    static boolean isRoutingScheme(String scheme) {
        return List.of(NEO4J_LOW_TRUST_URI_SCHEME, NEO4J_HIGH_TRUST_URI_SCHEME, NEO4J_URI_SCHEME)
                .contains(scheme);
    }
}
