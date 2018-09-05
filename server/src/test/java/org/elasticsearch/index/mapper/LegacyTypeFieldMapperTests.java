/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.index.mapper;

import org.apache.lucene.index.DocValuesType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexableField;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.compress.CompressedXContent;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.test.ESSingleNodeTestCase;

import java.io.IOException;

public class LegacyTypeFieldMapperTests extends ESSingleNodeTestCase {

    @Override
    protected boolean forbidPrivateIndexSettings() {
        return false;
    }

    public void testDocValuesMultipleTypes() throws Exception {
        TypeFieldMapperTests.testDocValues(index -> {
            final Settings settings = Settings.builder().put(IndexMetaData.SETTING_INDEX_VERSION_CREATED.getKey(), Version.V_5_6_0).build();
            return this.createIndex(index, settings);
        });
    }

    public void testDefaultsMultipleTypes() throws IOException {
        final Settings indexSettings = Settings.builder()
                .put("index.version.created", Version.V_5_6_0)
                .build();
        final MapperService mapperService = createIndex("test", indexSettings).mapperService();
        final DocumentMapper mapper =
                mapperService.merge("type", new CompressedXContent("{\"type\":{}}"), MapperService.MergeReason.MAPPING_UPDATE, false);
        final ParsedDocument document = mapper.parse(SourceToParse.source("index", "type", "id", new BytesArray("{}"), XContentType.JSON));
        final IndexableField[] fields = document.rootDoc().getFields(TypeFieldMapper.NAME);
        assertEquals(IndexOptions.DOCS, fields[0].fieldType().indexOptions());
        assertEquals(DocValuesType.SORTED_SET, fields[1].fieldType().docValuesType());
    }

}