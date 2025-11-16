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
 * Represents a blog author who can write multiple posts.
 * Demonstrates basic CRUD operations, searchable fields, one-to-many relationships,
 * List DTOs, and CrudTemplate.FULL with all endpoints enabled.
 */
@Entity
@CrudCrafted(editable = false, template = CrudTemplate.FULL)
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 100)
    @Dto({"List"})
    @Request
    @Searchable
    @ProjectionField("author.name")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank
    @Size(min = 5, max = 255)
    @Dto({"List"})
    @Request
    @Searchable
    @ProjectionField("author.email")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Dto
    @Request
    @Size(max = 500)
    @Column(length = 500)
    private String bio;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
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
