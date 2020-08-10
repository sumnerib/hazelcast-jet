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

package com.hazelcast.jet.sql.impl.connector.file;

import com.hazelcast.jet.sql.impl.schema.ExternalField;
import com.hazelcast.sql.impl.QueryException;
import com.hazelcast.sql.impl.schema.TableField;
import com.hazelcast.sql.impl.type.QueryDataType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

interface CsvMetadataResolver {

    static List<ExternalField> fields(String line, String delimiter) {
        String[] headers = line.split(delimiter);

        Map<String, ExternalField> fields = new LinkedHashMap<>();
        for (String header : headers) {
            ExternalField field = new ExternalField(header, QueryDataType.VARCHAR);

            fields.putIfAbsent(field.name(), field);
        }
        return new ArrayList<>(fields.values());
    }

    static List<TableField> fields(List<ExternalField> externalFields) {
        List<TableField> fields = new ArrayList<>();
        for (ExternalField externalField : externalFields) {
            String name = externalField.name();
            QueryDataType type = externalField.type();

            String externalName = externalField.externalName();
            if (externalName != null) {
                throw QueryException.error("External names are not supported");
            }

            TableField field = new FileTableField(name, type);

            fields.add(field);
        }
        return fields;
    }

    static Map<String, Integer> indices(List<TableField> fields) {
        return IntStream.range(0, fields.size()).boxed().collect(toMap(i -> fields.get(i).getName(), i -> i));
    }

    static String[] paths(List<TableField> fields) {
        return fields.stream().map(TableField::getName).toArray(String[]::new);
    }

    static QueryDataType[] types(List<TableField> fields) {
        return fields.stream().map(TableField::getType).toArray(QueryDataType[]::new);
    }
}