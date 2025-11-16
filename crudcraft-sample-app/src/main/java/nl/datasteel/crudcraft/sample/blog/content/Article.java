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
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;

/**
 * Concrete Article entity extending Content.
 * Demonstrates inheritance - this will generate full CRUD endpoints with fields
 * from both Article and the parent Content class (title, body, author, etc.).
 */
@Entity
@CrudCrafted
@Table(name = "articles")
public class Article extends Content {

    @Dto({"List"})
    @Request
    @Searchable
    @ProjectionField("article.subtitle")
    @Column(length = 200)
    private String subtitle;

    @Dto
    @Request
    @ProjectionField("article.readingTimeMinutes")
    @Column(name = "reading_time_minutes")
    private Integer readingTimeMinutes;

    @Dto({"List"})
    @Request
    @Column(name = "featured")
    private Boolean featured = false;

    @Dto
    @Request
    @Column(name = "allow_comments")
    private Boolean allowComments = true;

    // Getters and Setters
    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Integer getReadingTimeMinutes() {
        return readingTimeMinutes;
    }

    public void setReadingTimeMinutes(Integer readingTimeMinutes) {
        this.readingTimeMinutes = readingTimeMinutes;
    }

    public Boolean getFeatured() {
        return featured;
    }

    public void setFeatured(Boolean featured) {
        this.featured = featured;
    }

    public Boolean getAllowComments() {
        return allowComments;
    }

    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }
}
