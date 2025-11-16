/*
 * Copyright (c) 2025 CrudCraft contributors
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
// Copyright (c) 2025 CrudCraft contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package nl.datasteel.crudcraft.sample.blog.service;

import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.util.RelationshipUtils;
import nl.datasteel.crudcraft.sample.blog.Post;
import nl.datasteel.crudcraft.sample.blog.dto.ref.PostRef;
import nl.datasteel.crudcraft.sample.blog.dto.request.PostRequestDto;
import nl.datasteel.crudcraft.sample.blog.dto.response.PostResponseDto;
import nl.datasteel.crudcraft.sample.blog.mapper.PostMapper;
import nl.datasteel.crudcraft.sample.blog.repository.PostRepository;
import org.springframework.stereotype.Service;

/**
 * Generated Service layer stub for Post.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Service stub extends CrudCraft's base implementation. Override methods to customise behaviour.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (PostServiceBase)
 * which already implements full CRUD logic.
 *
 * This file was generated only once. CrudCraft will not overwrite it in future
 * builds. If you delete it, it will be regenerated.
 *
 * Features provided by CrudCraft:
 * - Standard CRUD workflow already implemented
 * - DTO mapping and repository calls wired up
 *
 * Generation context:
 * - Source model: Post
 * - Package: nl.datasteel.crudcraft.sample.blog.service
 * - Generator: ServiceGenerator
 * - Generation time: 2025-11-16T21:29:13.682623464Z
 * - CrudCraft version: null
 *
 * Recommendations:
 * - You may customize method behavior, add validation, or extend with additional endpoints.
 * - Signature changes are allowed, but may desync from service or mapper layer—proceed with care.
 * - Do not manually copy or paste other CrudCraft stubs into this class.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@Service
public class PostService extends AbstractCrudService<Post, PostRequestDto, PostResponseDto, PostRef, UUID> {
    public PostService(PostRepository repository, PostMapper mapper) {
        super(repository, mapper, Post.class, PostResponseDto.class, PostRef.class);
    }

    @Override
    protected void postSave(Post entity) {
        RelationshipUtils.fixBidirectional(entity);
    }

    @Override
    protected void preDelete(Post entity) {
        RelationshipUtils.clearBidirectional(entity);
    }
}
