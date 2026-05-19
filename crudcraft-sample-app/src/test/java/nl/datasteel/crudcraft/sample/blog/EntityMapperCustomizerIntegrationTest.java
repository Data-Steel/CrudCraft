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

package nl.datasteel.crudcraft.sample.blog;

import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.sample.blog.dto.request.PostRequestDto;
import nl.datasteel.crudcraft.sample.blog.dto.response.PostResponseDto;
import nl.datasteel.crudcraft.sample.blog.service.PostService;
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;


class EntityMapperCustomizerIntegrationTest extends PostgresIntegrationTestBase {

    @Autowired private PostService postService;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    @Tag("tck:mapper-customizer.generated-service")
    void generatedServiceAppliesEntityMapperCustomizerHooks() {
        SeedIds seed = seedIds();
        PostRequestDto request =
                new PostRequestDto(
                        "Mapper Customizer Post",
                        "Generated service mapper customizer content.",
                        PostService.MAPPER_CUSTOMIZER_TRIGGER,
                        seed.authorId(),
                        seed.categoryId(),
                        null,
                        Set.of(seed.tagId()),
                        null);

        PostResponseDto response = postService.create(request);

        assertEquals("mapper-customizer-entity-response", response.summary());
        assertEquals(
                PostService.MAPPER_CUSTOMIZER_ENTITY_SUMMARY,
                jdbcTemplate.queryForObject(
                        "select summary from posts where id = ?",
                        String.class,
                        response.id()));
    }

    private SeedIds seedIds() {
        UUID authorId = jdbcTemplate.queryForObject("select id from authors limit 1", UUID.class);
        UUID categoryId =
                jdbcTemplate.queryForObject("select id from categories limit 1", UUID.class);
        UUID tagId = jdbcTemplate.queryForObject("select id from tags limit 1", UUID.class);
        return new SeedIds(authorId, categoryId, tagId);
    }

    private record SeedIds(UUID authorId, UUID categoryId, UUID tagId) {}
}
