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
package demo.golden.include.search;

import demo.golden.include.AuditLog;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.search.SearchLogic;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generated model file for AuditLog; do not edit manually.
 * @CrudCraft:generated
 *
 * This search class reflects the @Searchable fields of your entity and should not be edited. It is a mutable, @NotThreadSafe command object intended for one request at a time.
 *
 * Included elements:
 * - Mutable search parameters with generated accessors
 * - Specification logic for filtering results
 * - Allowed path and operator metadata for request validation
 *
 * Example:
 * var request = new AuditLogSearchRequest();
 * request.setTitle("CrudCraft");
 * var spec = request.toSpecification();
 *
 * Generation context:
 * - Source model: AuditLog
 * - Package: demo.golden.include.search
 * - Generator: SearchGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
public class AuditLogSpecification implements Specification<AuditLog> {
    private static final long serialVersionUID = 1L;

    private final AuditLogSearchRequest request;

    public AuditLogSpecification(AuditLogSearchRequest request) {
        this.request = new AuditLogSearchRequest(request);
    }

    @Override
    public Predicate toPredicate(Root<AuditLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        SearchLogic logic = request.getSearchLogic();
        Predicate p = logic == SearchLogic.AND ? cb.conjunction() : cb.disjunction();
        boolean hasCriteria = false;
        if (request.getActor() != null && request.getActorOp() == SearchOperator.EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("actor").in(request.getActor())) : cb.or(p, root.get("actor").in(request.getActor()));
            hasCriteria = true;
        }
        if (request.getActor() != null && request.getActorOp() == SearchOperator.CONTAINS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("actor"), "%" + request.getActor() + "%")) : cb.or(p, cb.like(root.get("actor"), "%" + request.getActor() + "%"));
            hasCriteria = true;
        }
        if (request.getActor() != null && request.getActorOp() == SearchOperator.STARTS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("actor"), request.getActor() + "%")) : cb.or(p, cb.like(root.get("actor"), request.getActor() + "%"));
            hasCriteria = true;
        }
        if (request.getActor() != null && request.getActorOp() == SearchOperator.ENDS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("actor"), "%" + request.getActor())) : cb.or(p, cb.like(root.get("actor"), "%" + request.getActor()));
            hasCriteria = true;
        }
        if (request.getActor() != null && request.getActorOp() == SearchOperator.IN) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("actor").in(request.getActor())) : cb.or(p, root.get("actor").in(request.getActor()));
            hasCriteria = true;
        }
        if (request.getActor() != null && request.getActorOp() == SearchOperator.NOT_EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("actor").in(request.getActor()))) : cb.or(p, cb.not(root.get("actor").in(request.getActor())));
            hasCriteria = true;
        }
        if (request.getActor() != null && request.getActorOp() == SearchOperator.NOT_IN) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("actor").in(request.getActor()))) : cb.or(p, cb.not(root.get("actor").in(request.getActor())));
            hasCriteria = true;
        }
        if (request.getActor() != null && request.getActorOp() == SearchOperator.REGEX) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("actor"), request.getActor())) : cb.or(p, cb.like(root.get("actor"), request.getActor()));
            hasCriteria = true;
        }
        if (request.getHappenedAt() != null && request.getHappenedAtOp() == SearchOperator.EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("happenedAt").in(request.getHappenedAt())) : cb.or(p, root.get("happenedAt").in(request.getHappenedAt()));
            hasCriteria = true;
        }
        if (request.getHappenedAt() != null && request.getHappenedAtOp() == SearchOperator.BEFORE) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.lessThan(root.get("happenedAt"), request.getHappenedAt())) : cb.or(p, cb.lessThan(root.get("happenedAt"), request.getHappenedAt()));
            hasCriteria = true;
        }
        if (request.getHappenedAt() != null && request.getHappenedAtOp() == SearchOperator.AFTER) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.greaterThan(root.get("happenedAt"), request.getHappenedAt())) : cb.or(p, cb.greaterThan(root.get("happenedAt"), request.getHappenedAt()));
            hasCriteria = true;
        }
        if (request.getHappenedAtStart() != null && request.getHappenedAtEnd() != null && request.getHappenedAtOp() == SearchOperator.BETWEEN) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.between(root.get("happenedAt"), request.getHappenedAtStart(), request.getHappenedAtEnd())) : cb.or(p, cb.between(root.get("happenedAt"), request.getHappenedAtStart(), request.getHappenedAtEnd()));
            hasCriteria = true;
        }
        if (request.getHappenedAt() != null && request.getHappenedAtOp() == SearchOperator.GTE) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.greaterThanOrEqualTo(root.get("happenedAt"), request.getHappenedAt())) : cb.or(p, cb.greaterThanOrEqualTo(root.get("happenedAt"), request.getHappenedAt()));
            hasCriteria = true;
        }
        if (request.getHappenedAt() != null && request.getHappenedAtOp() == SearchOperator.LTE) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.lessThanOrEqualTo(root.get("happenedAt"), request.getHappenedAt())) : cb.or(p, cb.lessThanOrEqualTo(root.get("happenedAt"), request.getHappenedAt()));
            hasCriteria = true;
        }
        if (request.getHappenedAt() != null && request.getHappenedAtOp() == SearchOperator.NOT_EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("happenedAt").in(request.getHappenedAt()))) : cb.or(p, cb.not(root.get("happenedAt").in(request.getHappenedAt())));
            hasCriteria = true;
        }
        if (!hasCriteria && logic == SearchLogic.OR) {
            return cb.conjunction();
        }
        return p;
    }
}
