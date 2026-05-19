/*
 * Copyright (c) 2026 CrudCraft contributors
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

package nl.datasteel.crudcraft.codegen.writer;

import com.palantir.javapoet.JavaFile;
import java.util.List;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.writer.search.SearchField;
import nl.datasteel.crudcraft.codegen.writer.search.SearchFieldCollector;
import nl.datasteel.crudcraft.codegen.writer.search.SearchRequestGenerator;
import nl.datasteel.crudcraft.codegen.writer.search.SearchSpecificationGenerator;


/**
 * Orchestrates SearchRequest DTO and Specification generation for CrudCraft entities that have
 * searchable fields.
 */
public class SearchGenerator implements Generator {

    private static final int DEFAULT_GENERATION_DEPTH = 2;

    private final SearchRequestGenerator requestGenerator = new SearchRequestGenerator();
    private final SearchSpecificationGenerator specificationGenerator =
            new SearchSpecificationGenerator();

    /** Creates a search generator. */
    public SearchGenerator() {
        // Constructor without any parameters stays empty
    }

    /** Generates the SearchRequest DTO and Specification for the given model descriptor. */
    @Override
    public List<JavaFile> generate(ModelDescriptor md, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(md, ctx)) {
            return List.of();
        }

        List<SearchField> fields =
                List.copyOf(
                        new SearchFieldCollector(ctx).collect(md, DEFAULT_GENERATION_DEPTH));
        if (fields.isEmpty()) {
            return List.of();
        }

        return List.of(
                requestGenerator.generate(md, fields),
                specificationGenerator.generate(md, fields));
    }

    @Override
    public int order() {
        return 1;
    }
}
