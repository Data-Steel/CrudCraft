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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.runtime.extensions.AuditableExtension;

/**
 * Represents a category for organizing blog posts.
 * Demonstrates READ_ONLY template (no create/update/delete), projections, and List DTOs.
 */
@Entity
@CrudCrafted(editable = false, template = CrudTemplate.READ_ONLY)
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 50)
    @Dto({"List", "Map"})
    @Request
    @Searchable
    @ProjectionField("category.name")
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Dto
    @Request
    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts = new HashSet<>();

    @Embedded
    private AuditableExtension audit = new AuditableExtension();

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Post> getPosts() {
        return posts;
    }

    public void setPosts(Set<Post> posts) {
        this.posts = posts;
    }

    public AuditableExtension getAudit() {
        return audit;
    }

    public void setAudit(AuditableExtension audit) {
        this.audit = audit;
    }
}
