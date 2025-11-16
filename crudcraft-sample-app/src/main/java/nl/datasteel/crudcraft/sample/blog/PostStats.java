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
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.annotations.fields.Request;

/**
 * Represents statistics for a blog post.
 * Demonstrates one-to-one relationships and PATCH_ONLY template
 * (stats can only be modified via PATCH, not full replacement).
 */
@Entity
@CrudCrafted(editable = false, template = CrudTemplate.PATCH_ONLY)
@Table(name = "post_stats")
public class PostStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Dto
    private UUID id;

    @OneToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Dto({"List"})
    @Request
    @ProjectionField("stats.viewCount")
    @Column(nullable = false)
    private Integer viewCount = 0;

    @Dto({"List"})
    @Request
    @ProjectionField("stats.likeCount")
    @Column(nullable = false)
    private Integer likeCount = 0;

    @Dto({"List"})
    @Request
    @ProjectionField("stats.shareCount")
    @Column(nullable = false)
    private Integer shareCount = 0;

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

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getShareCount() {
        return shareCount;
    }

    public void setShareCount(Integer shareCount) {
        this.shareCount = shareCount;
    }
}
