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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import nl.datasteel.crudcraft.sample.tck.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GeneratedSourceContractIntegrationTest extends PostgresIntegrationTestBase {

    private final Path generatedSources = generatedSourcesRoot();

    @Test
    @Tag("tck:generated-source.artifacts")
    void generatedAuthorSurfaceContainsCrudDtoMapperServiceControllerAndSearchArtifacts() {
        assertGenerated("nl/datasteel/crudcraft/sample/blog/dto/request/AuthorRequestDto.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/dto/response/AuthorResponseDto.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/dto/response/AuthorListResponseDto.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/dto/ref/AuthorRef.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/mapper/AuthorMapper.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/mapper/AuthorMapperImpl.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/repository/AuthorRepository.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/service/AuthorService.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/controller/AuthorController.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/search/AuthorSearchRequest.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/search/AuthorSpecification.java");
        assertGenerated("nl/datasteel/crudcraft/sample/blog/meta/AuthorRelationshipMeta.java");
    }

    @Test
    @Tag("tck:dto.named-variants")
    @Tag("tck:relationship.one-to-many")
    @Tag("tck:relationship.one-to-one")
    @Tag("tck:relationship.many-to-many")
    void generatedDtoContractsExposeFieldsRelationsBuildersValidationAndSecurityMetadata()
            throws IOException {
        String request = read("nl/datasteel/crudcraft/sample/blog/dto/request/PostRequestDto.java");
        assertTrue(request.contains("public record PostRequestDto"));
        assertTrue(request.contains("UUID authorId"));
        assertTrue(request.contains("UUID categoryId"));
        assertTrue(request.contains("Set<UUID> tagIds"));
        assertTrue(request.contains("@NotBlank"));
        assertTrue(request.contains("@Size("));
        assertTrue(request.contains("FIELD_SECURITY_METADATA"));
        assertTrue(request.contains("public static Builder builder()"));
        assertTrue(request.contains("Set.copyOf(tagIds)"));
        assertFalse(request.contains("withAuthorId(UUID authorId)"));
        assertFalse(request.contains("Author author"));
        assertFalse(request.contains("Category category"));
        assertFalse(request.contains("Set<Tag> tags"));

        String response = read("nl/datasteel/crudcraft/sample/blog/dto/response/PostResponseDto.java");
        assertTrue(response.contains("public record PostResponseDto"));
        assertTrue(response.contains("AuthorRef author"));
        assertTrue(response.contains("CategoryRef category"));
        assertTrue(response.contains("Set<TagRef> tags"));
        assertTrue(response.contains("PostStatus status"));
        assertTrue(response.contains("@NotNull"));
        assertTrue(response.contains("@Nullable"));

        String ref = read("nl/datasteel/crudcraft/sample/blog/dto/ref/PostRef.java");
        assertTrue(ref.contains("public record PostRef"));
        assertTrue(ref.contains("UUID id"));
        assertFalse(ref.contains("String title"));
        assertFalse(ref.contains("String content"));
        assertFalse(ref.contains("Set<TagRef> tags"));
    }

    @Test
    @Tag("tck:template.search-only-source")
    @Tag("tck:template.meta-only-source")
    @Tag("tck:template.validation-only-source")
    @Tag("tck:template.no-batch-source")
    @Tag("tck:template.create-only-source")
    @Tag("tck:template.light-public-source")
    @Tag("tck:template.secure-internal-source")
    @Tag("tck:overlay.include-endpoints")
    @Tag("tck:overlay.omit-endpoints")
    void generatedControllerContractsReflectTemplatePolicyProjectionExportAndOpenApi()
            throws IOException {
        String author = read("nl/datasteel/crudcraft/sample/blog/controller/AuthorController.java");
        assertTrue(author.contains("@RequestMapping(\"/authors\")"));
        assertTrue(author.contains("private Pageable clampPageable(Pageable pageable)"));
        assertTrue(author.contains("@PostMapping"));
        assertTrue(author.contains("@GetMapping(\"/ref\")"));
        assertTrue(author.contains("@PostMapping(\"/batch\")"));
        assertTrue(author.contains("@PostMapping(\"/batch/upsert\")"));
        assertTrue(author.contains("@DeleteMapping(\"/batch/delete\")"));
        assertTrue(author.contains("@RequestMapping("));
        assertTrue(author.contains("method = {RequestMethod.HEAD, RequestMethod.GET}"));
        assertTrue(author.contains("@GetMapping(\"/count\")"));
        assertTrue(author.contains("@PostMapping(\"/validate\")"));
        assertTrue(author.contains("@GetMapping(\"/search\")"));
        assertTrue(author.contains("@GetMapping(\"/list\")"));
        assertTrue(author.contains("@GetMapping(\"/list/{id}\")"));
        assertTrue(author.contains("@Operation("));
        assertFalse(author.contains("@GetMapping(\"/export\")"));

        String category = read("nl/datasteel/crudcraft/sample/blog/controller/CategoryController.java");
        assertFalse(category.contains("public ResponseEntity<CategoryResponseDto> post"));
        assertFalse(category.contains("@PatchMapping"));
        assertFalse(category.contains("@DeleteMapping"));

        String tag = read("nl/datasteel/crudcraft/sample/blog/controller/TagController.java");
        assertTrue(tag.contains("@PostMapping"));
        assertFalse(tag.contains("@PatchMapping"));
        assertFalse(tag.contains("@DeleteMapping"));
    }

    @Test
    @Tag("tck:projection.registry")
    @Tag("tck:projection.field-path")
    void generatedSearchAndProjectionContractsExposeAllowedPathsOperatorsAndRegistry()
            throws IOException {
        String search = read("nl/datasteel/crudcraft/sample/blog/search/PostSearchRequest.java");
        assertTrue(search.contains("ALLOWED_SEARCH_PATHS"));
        assertTrue(search.contains("\"authorEmail\""));
        assertTrue(search.contains("\"tagsName\""));
        assertTrue(search.contains("ALLOWED_SORT_PATHS"));
        assertTrue(search.contains("ALLOWED_SEARCH_OPERATORS"));
        assertTrue(search.contains("SearchLogic.OR"));
        assertTrue(search.contains("requestedSearchCriteria"));
        assertFalse(search.contains("\"updatedAt\""));

        String registry =
                read(
                        "nl/datasteel/crudcraft/sample/projection/"
                                + "GeneratedProjectionMetadataRegistry.java");
        assertTrue(registry.contains("PostResponseDtoProjectionMetadata"));
        assertTrue(registry.contains("PostListResponseDtoProjectionMetadata"));
        assertTrue(registry.contains("UserResponseDtoProjectionMetadata"));
    }

    @Test
    void abstractAndLobGenerationKeepExpectedPositiveAndNegativeArtifacts()
            throws IOException {
        assertGenerated("nl/datasteel/crudcraft/sample/blog/content/dto/ref/ContentRef.java");
        assertNotGenerated("nl/datasteel/crudcraft/sample/blog/content/controller/ContentController.java");
        assertNotGenerated("nl/datasteel/crudcraft/sample/blog/content/service/ContentService.java");
        assertNotGenerated("nl/datasteel/crudcraft/sample/blog/content/repository/ContentRepository.java");
        assertNotGenerated("nl/datasteel/crudcraft/sample/blog/content/mapper/ContentMapper.java");

        String comment = read("nl/datasteel/crudcraft/sample/blog/controller/CommentController.java");
        assertTrue(comment.contains("consumes = MediaType.MULTIPART_FORM_DATA_VALUE"));
        assertTrue(comment.contains("@RequestPart(\"data\") CommentRequestDto request"));
        assertTrue(comment.contains("@RequestPart(value = \"attachment\", required = false)"));
        assertTrue(comment.contains("attachment.isEmpty()"));
        assertFalse(comment.contains("@DeleteMapping(\"/{id}\")"));
    }

    @Test
    void generatedSecurityAndScopedRowSecurityContractsArePresent() throws IOException {
        String userController =
                readSource("nl/datasteel/crudcraft/sample/user/controller/UserController.java");
        assertTrue(userController.contains("@PreAuthorize"));
        assertTrue(userController.contains("hasRole('ADMIN')"));
        assertFalse(userController.contains("permitAll"));
        assertEditableControllerUsesCurrentBulkAndFieldSecurityContract(userController);

        String postController =
                readSource("nl/datasteel/crudcraft/sample/blog/controller/PostController.java");
        assertEditableControllerUsesCurrentBulkAndFieldSecurityContract(postController);

        String userDto = read("nl/datasteel/crudcraft/sample/user/dto/response/UserResponseDto.java");
        assertTrue(userDto.contains("FieldRule<>(\"passwordHash\""));
        assertTrue(userDto.contains("true, List.of(), List.of(\"ALL\")"));
        assertTrue(userDto.contains("String passwordHash"));

        String scopedService =
                read("nl/datasteel/crudcraft/sample/scope/service/ScopedRecordService.java");
        assertTrue(scopedService.contains("PrincipalScopeAccessor principalScopeAccessor"));
        assertTrue(scopedService.contains("ClaimScopedRowSecurityHandler<>(\"tenant\""));
        assertTrue(scopedService.contains("ClaimScopedRowSecurityHandler<>(\"client\""));
        assertTrue(scopedService.contains("ClaimScopedRowSecurityHandler<>(\"owner\""));
        assertTrue(scopedService.contains("RowSecurityRuntimeExtension"));
    }

    private void assertEditableControllerUsesCurrentBulkAndFieldSecurityContract(
            String controllerSource) {
        assertTrue(controllerSource.contains("BulkResult<"));
        assertTrue(controllerSource.contains("service.createAllResult("));
        assertTrue(controllerSource.contains("service.updateAllResult("));
        assertTrue(controllerSource.contains("service.patchAllResult("));
        assertTrue(controllerSource.contains("service.upsertAllResult("));
        assertTrue(controllerSource.contains("service.deleteAllByIdsResult("));
        assertTrue(controllerSource.contains("request = FieldSecurityUtil.filterWrite(request);"));
        assertTrue(controllerSource.contains("FieldSecurityUtil.filterWrite(request);"));
        assertTrue(controllerSource.contains("new Identified<>("));
        assertFalse(controllerSource.contains("service.createAll(requests)"));
        assertFalse(controllerSource.contains("service.updateAll(requests)"));
        assertFalse(controllerSource.contains("service.patchAll(requests)"));
        assertFalse(controllerSource.contains("service.upsertAll(requests)"));
        assertFalse(controllerSource.contains("service.deleteAllByIds(ids)"));
        assertFalse(containsLine(controllerSource, "requests.forEach(FieldSecurityUtil::filterWrite);"));
        assertFalse(controllerSource.contains("requests.forEach(r -> FieldSecurityUtil.filterWrite"));
    }

    private boolean containsLine(String source, String line) {
        return source.lines().map(String::trim).anyMatch(line::equals);
    }

    private void assertGenerated(String relativePath) {
        assertTrue(Files.exists(generatedSources.resolve(relativePath)), relativePath);
    }

    private void assertNotGenerated(String relativePath) {
        assertFalse(Files.exists(generatedSources.resolve(relativePath)), relativePath);
    }

    private String read(String relativePath) throws IOException {
        return Files.readString(generatedSources.resolve(relativePath));
    }

    private String readSource(String relativePath) throws IOException {
        Path moduleSource = Path.of("src", "main", "java").resolve(relativePath);
        if (Files.exists(moduleSource)) {
            return Files.readString(moduleSource);
        }
        return Files.readString(
                Path.of("crudcraft-sample-app", "src", "main", "java").resolve(relativePath));
    }

    private static Path generatedSourcesRoot() {
        Path moduleRoot = Path.of("target", "generated-sources", "annotations");
        if (Files.exists(moduleRoot)) {
            return moduleRoot;
        }
        return Path.of("crudcraft-sample-app", "target", "generated-sources", "annotations");
    }
}
