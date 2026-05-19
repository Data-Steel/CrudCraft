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

package demo.golden.search;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.Searchable;

@Entity
@CrudCrafted
public class OperatorPlayground {
    @Id @Dto(ref = true) private UUID id;
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
                SearchOperator.NOT_IN,
                SearchOperator.IS_EMPTY,
                SearchOperator.NOT_EMPTY
            })
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
    private BigDecimal score;
    @Dto
    @Searchable(operators = {SearchOperator.BEFORE, SearchOperator.AFTER})
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
    private Set<String> labels;
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
    @ManyToMany
    private Set<SearchTag> tags;
    @Dto
    @Searchable(
            operators = {
                SearchOperator.CONTAINS_KEY,
                SearchOperator.CONTAINS_VALUE,
                SearchOperator.SIZE_EQUALS
            })
    @ElementCollection
    private Map<String, String> attributes;
}
