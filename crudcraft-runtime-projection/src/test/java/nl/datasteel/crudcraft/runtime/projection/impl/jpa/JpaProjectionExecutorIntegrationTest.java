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

package nl.datasteel.crudcraft.runtime.projection.impl.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.projection.ProjectionExecutionException;
import nl.datasteel.crudcraft.runtime.projection.api.FilterCriteria;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.runtime.projection.api.ProjectionResult;
import nl.datasteel.crudcraft.runtime.projection.mapping.SimpleProjectionMetadataRegistry;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadata;
import nl.datasteel.crudcraft.runtime.projection.metadata.ProjectionMetadataRegistry;
import nl.datasteel.crudcraft.runtime.service.projection.ProjectionAdapter;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = JpaProjectionExecutorIntegrationTest.TestApplication.class)
@Import(JpaProjectionExecutorIntegrationTest.TestConfig.class)
@Transactional
@TestPropertySource(
        properties = {
            "spring.main.allow-bean-definition-overriding=true",
            "spring.jpa.hibernate.ddl-auto=create-drop",
            "spring.jpa.properties.hibernate.session_factory.statement_inspector="
                + "nl.datasteel.crudcraft.runtime.projection.impl.jpa.JpaProjectionExecutorIntegrationTest$SqlCaptureInspector"
        })
public class JpaProjectionExecutorIntegrationTest {

    private static volatile boolean denyContentField;
    private static volatile boolean denyTagsField;

    private final jakarta.persistence.EntityManager entityManager;

    private final ProjectionAdapter projectionAdapter;

    private final ProjectionExecutor projectionExecutor;

    @Autowired
    public JpaProjectionExecutorIntegrationTest(
            jakarta.persistence.EntityManager entityManager,
            ProjectionAdapter projectionAdapter,
            ProjectionExecutor projectionExecutor) {
        this.entityManager = entityManager;
        this.projectionAdapter = projectionAdapter;
        this.projectionExecutor = projectionExecutor;
    }

    @BeforeEach
    void setUp() {
        denyContentField = false;
        denyTagsField = false;
        SqlCaptureInspector.clear();

        TagEntity tagA = new TagEntity("a");
        TagEntity tagB = new TagEntity("b");
        entityManager.persist(tagA);
        entityManager.persist(tagB);

        PostEntity first = new PostEntity("First", "Content 1", "ACTIVE");
        first.tags.add(tagA);
        first.tags.add(tagB);
        entityManager.persist(first);

        PostEntity second = new PostEntity("Second", "Content 2", "ACTIVE");
        second.tags.add(tagB);
        entityManager.persist(second);

        entityManager.flush();
        entityManager.clear();
        SqlCaptureInspector.clear();
    }

    @Test
    void listProjectionSelectsOnlyRequestedColumns() {
        List<PostListDto> projected =
                projectionAdapter.projectList(PostEntity.class, PostListDto.class, null);

        assertEquals(2, projected.size());
        String projectionSql = SqlCaptureInspector.firstSelect();
        assertNotNull(projectionSql);
        assertTrue(projectionSql.toLowerCase().contains("title"));
        assertTrue(!projectionSql.toLowerCase().contains("content"));
    }

    @Test
    void refProjectionSelectsRefColumnsOnly() {
        List<PostRefDto> projected =
                projectionAdapter.projectList(PostEntity.class, PostRefDto.class, null);

        assertEquals(2, projected.size());
        String projectionSql = SqlCaptureInspector.firstSelect();
        assertNotNull(projectionSql);
        String sql = projectionSql.toLowerCase();
        assertTrue(sql.contains("id"));
        assertTrue(sql.contains("title"));
        assertTrue(!sql.contains("content"));
    }

    @Test
    void joinHeavySearchUsesDistinctCount() {
        Specification<PostEntity> joinSpec =
                (root, query, cb) -> root.join("tags").get("name").in("a", "b");

        Page<PostListDto> page =
                projectionAdapter.projectPage(
                        PostEntity.class, PostListDto.class, joinSpec, PageRequest.of(0, 10));

        assertEquals(2, page.getContent().size());
        assertEquals(2, page.getTotalElements());
        assertTrue(
                SqlCaptureInspector.countSelects().stream()
                        .anyMatch(sql -> sql.toLowerCase().contains("count(distinct")));
    }

    @Test
    void deniedFieldIsExcludedFromProjectionSelection() {
        denyContentField = true;
        List<PostSearchDto> projected =
                projectionAdapter.projectList(PostEntity.class, PostSearchDto.class, null);

        assertEquals(2, projected.size());
        assertTrue(projected.stream().allMatch(dto -> dto.content() == null));
        String projectionSql = SqlCaptureInspector.firstSelect();
        assertNotNull(projectionSql);
        assertTrue(!projectionSql.toLowerCase().contains("content"));
    }

    @Test
    void collectionProjectionHydratesTags() {
        List<PostWithTagsDto> projected =
                projectionAdapter.projectList(PostEntity.class, PostWithTagsDto.class, null);

        assertEquals(2, projected.size());

        PostWithTagsDto first =
                projected.stream()
                        .filter(dto -> "First".equals(dto.title()))
                        .findFirst()
                        .orElseThrow();
        PostWithTagsDto second =
                projected.stream()
                        .filter(dto -> "Second".equals(dto.title()))
                        .findFirst()
                        .orElseThrow();

        assertEquals(List.of("a", "b"), first.tags().stream().map(TagDto::name).sorted().toList());
        assertEquals(List.of("b"), second.tags().stream().map(TagDto::name).sorted().toList());
    }

    @Test
    void directExecutorSupportsNullPageableAndNullPredicate() {
        FilterCriteria<PostEntity> filter =
                new FilterCriteria<>() {
                    @Override
                    public Optional<Specification<PostEntity>> asSpecification() {
                        return Optional.of((root, query, cb) -> null);
                    }
                };

        ProjectionResult<PostListDto> result =
                projectionExecutor.project(
                        PostEntity.class, PostListDto.class, ProjectionQuery.of(filter, null));

        assertEquals(2, result.content().size());
        assertEquals(2L, result.totalElements());
    }

    @Test
    void sortingDescendingIsApplied() {
        Page<PostListDto> page =
                projectionAdapter.projectPage(
                        PostEntity.class,
                        PostListDto.class,
                        null,
                        PageRequest.of(0, 10, Sort.by(Sort.Order.desc("title"))));

        assertEquals(List.of("Second", "First"), page.map(PostListDto::title).getContent());
    }

    @Test
    void sortingAscendingIsApplied() {
        Page<PostListDto> page =
                projectionAdapter.projectPage(
                        PostEntity.class,
                        PostListDto.class,
                        null,
                        PageRequest.of(0, 10, Sort.by(Sort.Order.asc("title"))));

        assertEquals(List.of("First", "Second"), page.map(PostListDto::title).getContent());
    }

    @Test
    void collectionProjectionSupportsSpecificationsWithPredicate() {
        Specification<PostEntity> spec =
                (root, query, cb) -> cb.equal(root.get("status"), "ACTIVE");

        List<PostWithTagsDto> projected =
                projectionAdapter.projectList(PostEntity.class, PostWithTagsDto.class, spec);

        assertEquals(2, projected.size());
        assertTrue(projected.stream().allMatch(dto -> !dto.tags().isEmpty()));
    }

    @Test
    void collectionProjectionSupportsSpecificationsWithNullPredicate() {
        FilterCriteria<PostEntity> filter =
                new FilterCriteria<>() {
                    @Override
                    public Optional<Specification<PostEntity>> asSpecification() {
                        return Optional.of((root, query, cb) -> null);
                    }
                };

        ProjectionResult<PostWithTagsDto> result =
                projectionExecutor.project(
                        PostEntity.class,
                        PostWithTagsDto.class,
                        ProjectionQuery.of(filter, PageRequest.of(0, 10)));

        assertEquals(2, result.content().size());
    }

    @Test
    void unregisteredProjectionFallsBackToReflectionMapping() {
        ProjectionResult<UnregisteredDto> result =
                projectionExecutor.project(
                        PostEntity.class,
                        UnregisteredDto.class,
                        ProjectionQuery.of(new FilterCriteria<>() {}, PageRequest.of(0, 10)));

        assertEquals(2, result.content().size());
        assertEquals(
                List.of("First", "Second"),
                result.content().stream().map(UnregisteredDto::title).toList());
    }

    @Test
    void directExecutorSupportsPageableWithNullSort() {
        ProjectionResult<PostListDto> result =
                projectionExecutor.project(
                        PostEntity.class,
                        PostListDto.class,
                        ProjectionQuery.of(new FilterCriteria<>() {}, new NullSortPageable()));

        assertEquals(2, result.content().size());
    }

    @Test
    void nonCollectionProjectionAppliesSpecificationAndPaging() {
        Specification<PostEntity> spec = (root, query, cb) -> cb.equal(root.get("title"), "First");

        Page<PostListDto> page =
                projectionAdapter.projectPage(
                        PostEntity.class, PostListDto.class, spec, PageRequest.of(0, 10));

        assertEquals(1, page.getContent().size());
        assertEquals("First", page.getContent().getFirst().title());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void nonCollectionProjectionAppliesPaging() {
        Page<PostListDto> page =
                projectionAdapter.projectPage(
                        PostEntity.class,
                        PostListDto.class,
                        null,
                        PageRequest.of(1, 1, Sort.by(Sort.Order.asc("title"))));

        assertEquals(1, page.getContent().size());
        assertEquals("Second", page.getContent().getFirst().title());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void collectionProjectionAppliesSpecificationAndCount() {
        Specification<PostEntity> spec = (root, query, cb) -> cb.equal(root.get("title"), "First");

        Page<PostWithTagsDto> page =
                projectionAdapter.projectPage(
                        PostEntity.class, PostWithTagsDto.class, spec, PageRequest.of(0, 10));

        assertEquals(1, page.getContent().size());
        assertEquals("First", page.getContent().getFirst().title());
        assertEquals(1L, page.getTotalElements());
    }

    @Test
    void collectionProjectionAppliesSortingAndPaging() {
        Page<PostWithTagsDto> page =
                projectionAdapter.projectPage(
                        PostEntity.class,
                        PostWithTagsDto.class,
                        null,
                        PageRequest.of(0, 1, Sort.by(Sort.Order.desc("title"))));

        assertEquals(1, page.getContent().size());
        assertEquals("Second", page.getContent().getFirst().title());
        assertEquals(2L, page.getTotalElements());
    }

    @Test
    void deniedCollectionFieldSkipsCollectionHydration() {
        denyTagsField = true;
        try {
            List<PostWithTagsDto> projected =
                    projectionAdapter.projectList(PostEntity.class, PostWithTagsDto.class, null);

            assertEquals(2, projected.size());
            assertTrue(projected.stream().allMatch(dto -> dto.tags().isEmpty()));
        } finally {
            denyTagsField = false;
        }
    }

    @Test
    void directExecutorAppliesSpecificationToCountQuery() {
        FilterCriteria<PostEntity> filter =
                FilterCriteria.ofSpecification(
                        (root, query, cb) -> cb.equal(root.get("title"), "First"));

        ProjectionResult<PostListDto> result =
                projectionExecutor.project(
                        PostEntity.class,
                        PostListDto.class,
                        ProjectionQuery.of(filter, PageRequest.of(0, 10)));

        assertEquals(1, result.content().size());
        assertEquals("First", result.content().getFirst().title());
        assertEquals(1L, result.totalElements());
    }

    @Test
    void collectionProjectionWithoutNestedMetadataFailsFast() {
        ProjectionExecutionException thrown =
                assertThrows(
                        ProjectionExecutionException.class,
                        () ->
                                projectionExecutor.project(
                                        PostEntity.class,
                                        PostWithInvalidTagsDto.class,
                                        ProjectionQuery.of(
                                                new FilterCriteria<>() {},
                                                PageRequest.of(0, 10))));

        assertEquals(PostWithInvalidTagsDto.class.getName(), thrown.getContext().get("dto"));
        assertEquals("tags", thrown.getContext().get("attribute"));
    }

    @Test
    void circularProjectionMetadataFailsBeforeQueryExecution() {
        ProjectionExecutionException thrown =
                assertThrows(
                        ProjectionExecutionException.class,
                        () ->
                                projectionExecutor.project(
                                        PostEntity.class,
                                        CircularProjectionDto.class,
                                        ProjectionQuery.of(new FilterCriteria<PostEntity>() {}, null)));

        assertTrue(thrown.getMessage().contains("Circular projection metadata detected"));
        assertEquals(CircularProjectionDto.class.getName(), thrown.getContext().get("cycle"));
        assertTrue(SqlCaptureInspector.statements().isEmpty());
    }

    record PostListDto(String title) {}

    record PostRefDto(Long id, String title) {}

    record PostSearchDto(String title, String content) {}

    static final class PostWithTagsDto {
        private final String title;
        private List<TagDto> tags = List.of();

        PostWithTagsDto(String title) {
            this.title = title;
        }

        String title() {
            return title;
        }

        List<TagDto> tags() {
            return tags;
        }
    }

    static final class PostWithInvalidTagsDto {
        private final String title;
        private List<Object> tags = List.of();

        PostWithInvalidTagsDto(String title) {
            this.title = title;
        }

        String title() {
            return title;
        }
    }

    record TagDto(String name) {}

    record UnregisteredDto(String title) {}

    @Entity(name = "projection_post")
    @Table(name = "projection_posts")
    static class PostEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        String title;
        String content;
        String status;

        @ManyToMany Set<TagEntity> tags = new LinkedHashSet<>();

        PostEntity() {}

        PostEntity(String title, String content, String status) {
            this.title = title;
            this.content = content;
            this.status = status;
        }
    }

    @Entity(name = "projection_tag")
    @Table(name = "projection_tags")
    static class TagEntity {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        Long id;

        String name;

        TagEntity() {}

        TagEntity(String name) {
            this.name = name;
        }
    }

    public static class SqlCaptureInspector implements StatementInspector {
        private static final List<String> SQL = new ArrayList<>();

        @Override
        public String inspect(String sql) {
            SQL.add(sql);
            return sql;
        }

        static void clear() {
            SQL.clear();
        }

        static String firstSelect() {
            return SQL.stream()
                    .filter(sql -> sql.toLowerCase().startsWith("select"))
                    .filter(sql -> !sql.toLowerCase().contains("count("))
                    .findFirst()
                    .orElse(null);
        }

        static List<String> countSelects() {
            return SQL.stream().filter(sql -> sql.toLowerCase().contains("count(")).toList();
        }

        static List<String> statements() {
            return List.copyOf(SQL);
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class TestConfig {

        @Bean
        @Primary
        public ProjectionMetadataRegistry testProjectionMetadataRegistry() {
            SimpleProjectionMetadataRegistry registry = new SimpleProjectionMetadataRegistry();
            registry.register(new PostListProjectionMetadata());
            registry.register(new PostRefProjectionMetadata());
            registry.register(new PostSearchProjectionMetadata());
            registry.register(new TagProjectionMetadata());
            registry.register(new PostWithTagsProjectionMetadata());
            registry.register(new PostWithInvalidTagsProjectionMetadata());
            registry.register(new CircularProjectionMetadata());
            return registry;
        }

        @Bean
        public FieldSecurityAdapter fieldSecurityAdapter() {
            return new ToggleFieldSecurityAdapter();
        }

        @Bean
        public CriteriaProjectionBuilder criteriaProjectionBuilder(
                ProjectionMetadataRegistry registry, FieldSecurityAdapter fieldSecurityAdapter) {
            return new MetadataCriteriaProjectionBuilder(registry, fieldSecurityAdapter);
        }

        @Bean
        public ProjectionExecutor projectionExecutor(
                jakarta.persistence.EntityManager entityManager,
                CriteriaProjectionBuilder criteriaProjectionBuilder,
                ProjectionMetadataRegistry registry,
                FieldSecurityAdapter fieldSecurityAdapter) {
            return new JpaProjectionExecutor(
                    entityManager, criteriaProjectionBuilder, registry, fieldSecurityAdapter);
        }

        @Bean
        public ProjectionAdapter projectionAdapter(
                ProjectionExecutor projectionExecutor, ProjectionMetadataRegistry registry) {
            return new JpaCriteriaProjectionAdapter(projectionExecutor, registry);
        }
    }

    @SpringBootApplication
    @EntityScan(basePackageClasses = JpaProjectionExecutorIntegrationTest.class)
    public static class TestApplication {}

    static final class PostListProjectionMetadata implements ProjectionMetadata<PostListDto> {

        @Override
        public Class<PostListDto> dtoType() {
            return PostListDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(new BasicAttribute("title", "title"));
        }
    }

    static final class PostRefProjectionMetadata implements ProjectionMetadata<PostRefDto> {

        @Override
        public Class<PostRefDto> dtoType() {
            return PostRefDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(new BasicAttribute("id", "id"), new BasicAttribute("title", "title"));
        }
    }

    static final class PostSearchProjectionMetadata implements ProjectionMetadata<PostSearchDto> {

        @Override
        public Class<PostSearchDto> dtoType() {
            return PostSearchDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(
                    new BasicAttribute("title", "title"), new BasicAttribute("content", "content"));
        }
    }

    static final class TagProjectionMetadata implements ProjectionMetadata<TagDto> {

        @Override
        public Class<TagDto> dtoType() {
            return TagDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(new BasicAttribute("name", "name"));
        }
    }

    static final class PostWithTagsProjectionMetadata
            implements ProjectionMetadata<PostWithTagsDto> {

        @Override
        public Class<PostWithTagsDto> dtoType() {
            return PostWithTagsDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(
                    new BasicAttribute("title", "title"),
                    new CollectionAttribute(
                            "tags",
                            "tags",
                            new TagProjectionMetadata(),
                            (dto, values) -> ((PostWithTagsDto) dto).tags = cast(values)));
        }
    }

    static final class PostWithInvalidTagsProjectionMetadata
            implements ProjectionMetadata<PostWithInvalidTagsDto> {

        @Override
        public Class<PostWithInvalidTagsDto> dtoType() {
            return PostWithInvalidTagsDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(
                    new BasicAttribute("title", "title"),
                    new CollectionAttribute(
                            "tags",
                            "tags",
                            null,
                            (dto, values) -> ((PostWithInvalidTagsDto) dto).tags = cast(values)));
        }
    }

    record CircularProjectionDto(String title) {}

    static final class CircularProjectionMetadata
            implements ProjectionMetadata<CircularProjectionDto> {

        @Override
        public Class<CircularProjectionDto> dtoType() {
            return CircularProjectionDto.class;
        }

        @Override
        public List<Attribute> attributes() {
            return List.of(new NestedAttribute("title", "title", this));
        }
    }

    record BasicAttribute(String dtoFieldName, String path)
            implements ProjectionMetadata.Attribute {
        @Override
        public ProjectionMetadata<?> nested() {
            return null;
        }

        @Override
        public boolean collection() {
            return false;
        }

        @Override
        public java.util.function.BiConsumer<Object, List<?>> mutator() {
            return null;
        }
    }

    record CollectionAttribute(
            String dtoFieldName,
            String path,
            ProjectionMetadata<?> nested,
            java.util.function.BiConsumer<Object, List<?>> mutator)
            implements ProjectionMetadata.Attribute {

        @Override
        public boolean collection() {
            return true;
        }
    }

    record NestedAttribute(String dtoFieldName, String path, ProjectionMetadata<?> nested)
            implements ProjectionMetadata.Attribute {
        @Override
        public boolean collection() {
            return false;
        }

        @Override
        public java.util.function.BiConsumer<Object, List<?>> mutator() {
            return null;
        }
    }

    static final class ToggleFieldSecurityAdapter implements FieldSecurityAdapter {
        @Override
        public boolean canReadField(Class<?> dtoType, String fieldName) {
            if (denyTagsField
                    && dtoType.equals(PostWithTagsDto.class)
                    && "tags".equals(fieldName)) {
                return false;
            }
            return !(denyContentField
                    && dtoType.equals(PostSearchDto.class)
                    && "content".equals(fieldName));
        }
    }

    static final class NullSortPageable implements Pageable {
        @Override
        public int getPageNumber() {
            return 0;
        }

        @Override
        public int getPageSize() {
            return 10;
        }

        @Override
        public long getOffset() {
            return 0;
        }

        @Override
        public Sort getSort() {
            return null;
        }

        @Override
        public Pageable next() {
            return this;
        }

        @Override
        public Pageable previousOrFirst() {
            return this;
        }

        @Override
        public Pageable first() {
            return this;
        }

        @Override
        public Pageable withPage(int pageNumber) {
            return this;
        }

        @Override
        public boolean hasPrevious() {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> cast(List<?> values) {
        return (List<T>) values;
    }
}
