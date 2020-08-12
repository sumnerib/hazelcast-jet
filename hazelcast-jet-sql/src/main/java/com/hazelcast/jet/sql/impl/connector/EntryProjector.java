/*
 * Copyright (c) 2008-2020, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.jet.sql.impl.connector;

import com.hazelcast.sql.impl.extract.QueryPath;
import com.hazelcast.sql.impl.inject.UpsertInjector;
import com.hazelcast.sql.impl.inject.UpsertTarget;
import com.hazelcast.sql.impl.type.QueryDataType;

import java.util.Map.Entry;

import static com.hazelcast.jet.Util.entry;
import static com.hazelcast.jet.sql.impl.type.converter.ToConverters.getToConverter;

class EntryProjector {

    private final UpsertTarget keyTarget;
    private final UpsertTarget valueTarget;

    private final QueryDataType[] types;

    private final UpsertInjector[] injectors;

    EntryProjector(
            UpsertTarget keyTarget,
            UpsertTarget valueTarget,
            QueryPath[] paths,
            QueryDataType[] types
    ) {
        this.keyTarget = keyTarget;
        this.valueTarget = valueTarget;

        this.types = types;

        this.injectors = createInjectors(keyTarget, valueTarget, paths);
    }

    private static UpsertInjector[] createInjectors(
            UpsertTarget keyTarget,
            UpsertTarget valueTarget,
            QueryPath[] paths
    ) {
        UpsertInjector[] injectors = new UpsertInjector[paths.length];
        for (int i = 0; i < paths.length; i++) {
            QueryPath path = paths[i];
            injectors[i] = path.isKey()
                    ? keyTarget.createInjector(path.getPath())
                    : valueTarget.createInjector(path.getPath());
        }
        return injectors;
    }

    Entry<Object, Object> project(Object[] row) {
        keyTarget.init();
        valueTarget.init();
        for (int i = 0; i < row.length; i++) {
            Object value = getToConverter(types[i]).convert(row[i]);
            injectors[i].set(value);
        }
        return entry(keyTarget.conclude(), valueTarget.conclude());
    }
}
