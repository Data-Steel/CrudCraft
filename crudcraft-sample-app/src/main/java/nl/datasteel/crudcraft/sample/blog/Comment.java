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
package nl.datasteel.crudcraft.sample.blog;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.runtime.extensions.AuditableExtension;

/**
 * Represents a comment on a blog post.
 * Demonstrates many-to-one relationships, validation, and NO_DELETE template
 * (comments can be created and modified but not deleted for audit purposes).
 */
@Entity
@CrudCrafted(editable = false, template = CrudTemplate.NO_DELETE)
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto
    private UUID id;

    @Dto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @NotBlank
    @Size(min = 2, max = 100)
    @Dto({"List"})
    @Request
    @Searchable
    @ProjectionField("comment.authorName")
    @Column(nullable = false, length = 100)
    private String authorName;

    @NotBlank
    @Size(min = 5, max = 255)
    @Dto
    @Request
    @ProjectionField("comment.authorEmail")
    @Column(nullable = false, length = 255)
    private String authorEmail;

    @NotBlank
    @Size(min = 1, max = 2000)
    @Dto
    @Request
    @Searchable
    @ProjectionField("comment.content")
    @Column(nullable = false, length = 2000)
    private String content;

    @Dto
    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Dto
    @Column(nullable = false)
    private Boolean approved = false;

    @Embedded
    private AuditableExtension audit = new AuditableExtension();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public AuditableExtension getAudit() {
        return audit;
    }

    public void setAudit(AuditableExtension audit) {
        this.audit = audit;
    }
}
