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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;

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
    @Searchable
    private UUID id;

    @Dto
    @Searchable
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
    @Searchable
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Dto
    @Column(nullable = false)
    private Boolean approved = false;

    /**
     * Attachment file data. Demonstrates @Lob support for large objects.
     * LOB fields are lazy loaded and can be uploaded via Request DTOs.
     */
    @Dto
    @Request
    @Lob
    @jakarta.persistence.Basic(fetch = jakarta.persistence.FetchType.LAZY)
    @Column(name = "attachment")
    private byte[] attachment;

    /**
     * The timestamp when the entity was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Hooks into the JPA lifecycle to set the createdAt and updatedAt timestamps
     * when the entity is first persisted.
     */
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Hooks into the JPA lifecycle to update the updatedAt timestamp
     * whenever the entity is updated.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
