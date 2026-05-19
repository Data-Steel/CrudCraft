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
package demo.golden.omit.service;

import demo.golden.omit.Article;
import demo.golden.omit.dto.ref.ArticleRef;
import demo.golden.omit.dto.request.ArticleRequestDto;
import demo.golden.omit.dto.response.ArticleResponseDto;
import demo.golden.omit.mapper.ArticleMapper;
import demo.golden.omit.meta.ArticleRelationshipMeta;
import demo.golden.omit.repository.ArticleRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for Article; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Article
 * - Package: demo.golden.omit.service
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
public class ArticleService extends AbstractCrudService<Article, ArticleRequestDto, ArticleResponseDto, ArticleRef, UUID> {
    public ArticleService(ArticleRepository repository, ArticleMapper mapper) {
        super(repository, mapper, Article.class, ArticleResponseDto.class, ArticleRef.class);
    }

    @Override
    protected void postSave(Article entity) {
        ArticleRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Article entity) {
        ArticleRelationshipMeta.clear(entity);
    }
}
