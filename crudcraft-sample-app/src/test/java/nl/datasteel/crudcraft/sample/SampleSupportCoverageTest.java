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

package nl.datasteel.crudcraft.sample;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.OffsetDateTime;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.runtime.exception.ForbiddenException;
import nl.datasteel.crudcraft.sample.blog.Post;
import nl.datasteel.crudcraft.sample.blog.PostStatus;
import nl.datasteel.crudcraft.sample.blog.extension.EditorialCrudSecurityPolicy;
import nl.datasteel.crudcraft.sample.blog.extension.PostPublishingExtension;
import nl.datasteel.crudcraft.sample.blog.extension.PostRowSecurityHandler;
import nl.datasteel.crudcraft.sample.security.SecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class SampleSupportCoverageTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void editorialPolicyMapsEveryEndpointGroup() {
        EditorialCrudSecurityPolicy policy = new EditorialCrudSecurityPolicy();

        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.GET_ALL));
        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.GET_ALL_REF));
        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.GET_ONE));
        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.FIND_BY_IDS));
        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.SEARCH));
        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.COUNT));
        assertEquals("permitAll()", policy.getSecurityExpression(CrudEndpoint.EXISTS));
        assertEquals(
                "hasAnyRole('EDITOR','ADMIN')",
                policy.getSecurityExpression(CrudEndpoint.POST));
        assertEquals(
                "hasAnyRole('EDITOR','ADMIN')",
                policy.getSecurityExpression(CrudEndpoint.PUT));
        assertEquals(
                "hasAnyRole('EDITOR','ADMIN')",
                policy.getSecurityExpression(CrudEndpoint.PATCH));
        assertEquals(
                "hasAnyRole('EDITOR','ADMIN')",
                policy.getSecurityExpression(CrudEndpoint.BULK_CREATE));
        assertEquals(
                "hasAnyRole('EDITOR','ADMIN')",
                policy.getSecurityExpression(CrudEndpoint.BULK_UPDATE));
        assertEquals(
                "hasAnyRole('EDITOR','ADMIN')",
                policy.getSecurityExpression(CrudEndpoint.BULK_PATCH));
        assertEquals(
                "hasAnyRole('EDITOR','ADMIN')",
                policy.getSecurityExpression(CrudEndpoint.BULK_UPSERT));
        assertEquals(
                "hasAnyRole('EDITOR','ADMIN')",
                policy.getSecurityExpression(CrudEndpoint.VALIDATE));
        assertEquals("hasRole('ADMIN')", policy.getSecurityExpression(CrudEndpoint.DELETE));
        assertEquals("hasRole('ADMIN')", policy.getSecurityExpression(CrudEndpoint.BULK_DELETE));
        assertEquals("hasRole('REPORTING')", policy.getSecurityExpression(CrudEndpoint.EXPORT));
    }

    @Test
    void postRowSecurityRejectsArchivedRowsAndBuildsCriteriaPredicate() {
        PostRowSecurityHandler handler = new PostRowSecurityHandler();
        Post archived = new Post();
        archived.setStatus(PostStatus.ARCHIVED);
        Post draft = new Post();
        draft.setStatus(PostStatus.DRAFT);
        Root<Post> root = mock();
        @SuppressWarnings("unchecked")
        Path<PostStatus> statusPath = mock(Path.class);
        CriteriaBuilder criteriaBuilder = mock();
        Predicate predicate = mock();

        when(root.<PostStatus>get("status")).thenReturn(statusPath);
        when(criteriaBuilder.notEqual(statusPath, PostStatus.ARCHIVED)).thenReturn(predicate);

        assertThrows(ForbiddenException.class, () -> handler.apply(archived));
        handler.apply(draft);
        assertSame(predicate, handler.rowFilter().toPredicate(root, null, criteriaBuilder));
    }

    @Test
    void postPublishingExtensionOnlySetsMissingPublicationTimestamp() {
        PostPublishingExtension extension = new PostPublishingExtension();
        Post published = new Post();
        published.setStatus(PostStatus.PUBLISHED);
        Post alreadyPublished = new Post();
        alreadyPublished.setStatus(PostStatus.PUBLISHED);
        OffsetDateTime existing = OffsetDateTime.now().minusDays(1);
        alreadyPublished.setPublishedAt(existing);
        Post draft = new Post();
        draft.setStatus(PostStatus.DRAFT);

        extension.beforeSave(published);
        extension.beforeSave(alreadyPublished);
        extension.beforeSave(draft);

        assertNotNull(published.getPublishedAt());
        assertEquals(existing, alreadyPublished.getPublishedAt());
        assertNull(draft.getPublishedAt());
    }

    @Test
    void securityUtilReadsAuthenticationNameAndUuid() {
        UUID id = UUID.randomUUID();

        assertNull(SecurityUtil.currentUsername());
        assertNull(SecurityUtil.currentUserId());

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(id.toString(), null));
        assertEquals(id.toString(), SecurityUtil.currentUsername());
        assertEquals(id, SecurityUtil.currentUserId());

        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin", null));
        assertEquals("admin", SecurityUtil.currentUsername());
        assertNull(SecurityUtil.currentUserId());
        assertTrue(SecurityUtil.currentUsername().length() > 0);
    }
}
