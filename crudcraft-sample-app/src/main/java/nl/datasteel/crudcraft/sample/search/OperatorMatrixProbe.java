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

package nl.datasteel.crudcraft.sample.search;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Searchable;


/** Dedicated sample entity for exercising every generated search-operator family. */
@CrudCrafted
@Entity
@Table(name = "operator_matrix_probes")
public class OperatorMatrixProbe {

    @Dto(ref = true)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Dto
    @Searchable(
            operators = {
                SearchOperator.EQUALS,
                SearchOperator.NOT_EQUALS,
                SearchOperator.CONTAINS,
                SearchOperator.STARTS_WITH,
                SearchOperator.ENDS_WITH,
                SearchOperator.REGEX,
                SearchOperator.IN,
                SearchOperator.NOT_IN
            })
    @Column(nullable = false)
    private String title;

    @Dto
    @Searchable(
            operators = {
                SearchOperator.GT,
                SearchOperator.GTE,
                SearchOperator.LT,
                SearchOperator.LTE,
                SearchOperator.RANGE,
                SearchOperator.BETWEEN
            })
    @Column(nullable = false)
    private BigDecimal score;

    @Dto
    @Searchable(operators = {SearchOperator.BEFORE, SearchOperator.AFTER})
    @Column(nullable = false)
    private Instant publishedAt;

    @Dto
    @Searchable(
            operators = {
                SearchOperator.IS_EMPTY,
                SearchOperator.NOT_EMPTY,
                SearchOperator.SIZE_EQUALS,
                SearchOperator.SIZE_GT,
                SearchOperator.SIZE_LT,
                SearchOperator.CONTAINS_ALL
            })
    @ElementCollection
    @CollectionTable(
            name = "operator_matrix_probe_labels",
            joinColumns = @JoinColumn(name = "probe_id"))
    @Column(name = "label")
    private Set<String> labels = new LinkedHashSet<>();

    @Dto
    @Searchable(operators = {SearchOperator.CONTAINS_KEY, SearchOperator.CONTAINS_VALUE})
    @ElementCollection
    @CollectionTable(
            name = "operator_matrix_probe_attributes",
            joinColumns = @JoinColumn(name = "probe_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new LinkedHashMap<>();

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

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Set<String> getLabels() {
        return labels == null ? Set.of() : Set.copyOf(labels);
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels == null ? new LinkedHashSet<>() : new LinkedHashSet<>(labels);
    }

    public Map<String, String> getAttributes() {
        return attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes =
                attributes == null ? new LinkedHashMap<>() : new LinkedHashMap<>(attributes);
    }
}
