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
package nl.datasteel.crudcraft.sample.blog.content;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.sample.blog.Author;
import nl.datasteel.crudcraft.runtime.extensions.AuditableExtension;

/**
 * Abstract base class for all content items (articles, tutorials, reviews, etc.).
 * Demonstrates inheritance with CrudCraft - this abstract class is marked @CrudCrafted
 * but won't generate controller/service stubs. Child classes will include these fields
 * in their generated DTOs and endpoints.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "content")
@CrudCrafted
public abstract class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto
    private UUID id;

    @NotBlank
    @Size(min = 5, max = 200)
    @Dto({"List"})
    @Request
    @Searchable
    @ProjectionField("content.title")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Size(min = 10, max = 10000)
    @Dto
    @Request
    @Searchable
    @ProjectionField("content.body")
    @Column(nullable = false, length = 10000)
    private String body;

    @Dto
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @Dto({"List"})
    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Dto
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status = ContentStatus.DRAFT;

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
    }
}
