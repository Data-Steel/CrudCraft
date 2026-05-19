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
package demo.golden.search.service;

import demo.golden.search.SearchTag;
import demo.golden.search.dto.ref.SearchTagRef;
import demo.golden.search.dto.request.SearchTagRequestDto;
import demo.golden.search.dto.response.SearchTagResponseDto;
import demo.golden.search.mapper.SearchTagMapper;
import demo.golden.search.meta.SearchTagRelationshipMeta;
import demo.golden.search.repository.SearchTagRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for SearchTag; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: SearchTag
 * - Package: demo.golden.search.service
 * - Generator: ServiceGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@Service
public class SearchTagService extends AbstractCrudService<SearchTag, SearchTagRequestDto, SearchTagResponseDto, SearchTagRef, UUID> {
    public SearchTagService(SearchTagRepository repository, SearchTagMapper mapper) {
        super(repository, mapper, SearchTag.class, SearchTagResponseDto.class, SearchTagRef.class);
    }

    @Override
    protected void postSave(SearchTag entity) {
        SearchTagRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(SearchTag entity) {
        SearchTagRelationshipMeta.clear(entity);
    }
}
