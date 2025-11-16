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
package nl.datasteel.crudcraft.sample.blog.mapper;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import nl.datasteel.crudcraft.runtime.exception.MapperException;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import nl.datasteel.crudcraft.sample.blog.Author;
import nl.datasteel.crudcraft.sample.blog.Category;
import nl.datasteel.crudcraft.sample.blog.Post;
import nl.datasteel.crudcraft.sample.blog.PostStats;
import nl.datasteel.crudcraft.sample.blog.Tag;
import nl.datasteel.crudcraft.sample.blog.dto.ref.AuthorRef;
import nl.datasteel.crudcraft.sample.blog.dto.ref.CategoryRef;
import nl.datasteel.crudcraft.sample.blog.dto.ref.PostRef;
import nl.datasteel.crudcraft.sample.blog.dto.request.PostRequestDto;
import nl.datasteel.crudcraft.sample.blog.dto.response.PostListResponseDto;
import nl.datasteel.crudcraft.sample.blog.dto.response.PostResponseDto;
import org.mapstruct.BeanMapping;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.BeanWrapperImpl;

/**
 * Generated Mapper layer stub for Post.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Mapper stub extends CrudCraft's base implementation. Override methods to customise behaviour.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (PostMapperBase)
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
 * - Package: nl.datasteel.crudcraft.sample.blog.mapper
 * - Generator: MapperGenerator
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
@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE,
        injectionStrategy = InjectionStrategy.FIELD,
        uses = {TagMapper.class, PostStatsMapper.class, CommentMapper.class}
)
public interface PostMapper extends EntityMapper<Post, PostRequestDto, PostResponseDto, PostRef, UUID> {
    @Override
    @Mapping(
            target = "author",
            source = "authorId",
            qualifiedByName = "PostMapAuthor"
    )
    @Mapping(
            target = "category",
            source = "categoryId",
            qualifiedByName = "PostMapCategory"
    )
    @Mapping(
            target = "tags",
            source = "tagIds",
            qualifiedByName = "PostMapTagSet"
    )
    @Mapping(
            target = "stats",
            source = "statsId",
            qualifiedByName = "PostMapPostStats"
    )
    Post fromRequest(PostRequestDto request);

    @Override
    @Mapping(
            target = "author",
            source = "authorId",
            qualifiedByName = "PostMapAuthor"
    )
    @Mapping(
            target = "category",
            source = "categoryId",
            qualifiedByName = "PostMapCategory"
    )
    @Mapping(
            target = "tags",
            source = "tagIds",
            qualifiedByName = "PostMapTagSet"
    )
    @Mapping(
            target = "stats",
            source = "statsId",
            qualifiedByName = "PostMapPostStats"
    )
    Post update(@MappingTarget Post entity, PostRequestDto request);

    @Override
    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    @Mapping(
            target = "author",
            source = "authorId",
            qualifiedByName = "PostMapAuthor"
    )
    @Mapping(
            target = "category",
            source = "categoryId",
            qualifiedByName = "PostMapCategory"
    )
    @Mapping(
            target = "tags",
            source = "tagIds",
            qualifiedByName = "PostMapTagSet"
    )
    @Mapping(
            target = "stats",
            source = "statsId",
            qualifiedByName = "PostMapPostStats"
    )
    Post patch(@MappingTarget Post entity, PostRequestDto request);

    @Override
    @Mapping(
            target = "author",
            qualifiedByName = "PostToAuthorRef"
    )
    @Mapping(
            target = "category",
            qualifiedByName = "PostToCategoryRef"
    )
    PostResponseDto toResponse(Post entity);

    @Override
    PostRef toRef(Post entity);

    @Override
    default UUID getIdFromRequest(PostRequestDto request) {
        try {
            var wrapper = new BeanWrapperImpl(request);
            Object idVal = wrapper.getPropertyValue("id");
            return (UUID) idVal;
        } catch (Exception e) {
            throw new MapperException("Failed to read 'id' property from request DTO: " + request.getClass(), e);
        }
    }

    @Named("PostToAuthorRef")
    AuthorRef toAuthorRef(Author author);

    @Named("PostToCategoryRef")
    CategoryRef toCategoryRef(Category category);

    @Named("PostMapAuthor")
    default Author mapAuthor(UUID id) {
        if (id == null) {
            return null;
        }
        Author entity = new Author();
        entity.setId(id);
        return entity;
    }

    @Named("PostMapCategory")
    default Category mapCategory(UUID id) {
        if (id == null) {
            return null;
        }
        Category entity = new Category();
        entity.setId(id);
        return entity;
    }

    @Named("PostMapTag")
    default Tag mapTag(UUID id) {
        if (id == null) {
            return null;
        }
        Tag entity = new Tag();
        entity.setId(id);
        return entity;
    }

    @Named("PostMapTagSet")
    default Set<Tag> mapTagSet(Set<UUID> ids) {
        if (ids == null) {
            return null;
        }
        return ids.stream().map(this::mapTag).collect(Collectors.toSet());
    }

    @Named("PostMapPostStats")
    default PostStats mapPostStats(UUID id) {
        if (id == null) {
            return null;
        }
        PostStats entity = new PostStats();
        entity.setId(id);
        return entity;
    }

    PostListResponseDto toListResponse(Post entity);
}
