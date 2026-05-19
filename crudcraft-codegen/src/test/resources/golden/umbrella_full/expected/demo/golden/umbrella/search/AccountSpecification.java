package demo.golden.umbrella.search;

import demo.golden.umbrella.Account;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.search.SearchLogic;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generated model file for Account; do not edit manually.
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
 * var request = new AccountSearchRequest();
 * request.setTitle("CrudCraft");
 * var spec = request.toSpecification();
 *
 * Generation context:
 * - Source model: Account
 * - Package: demo.golden.umbrella.search
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
public class AccountSpecification implements Specification<Account> {
    private static final long serialVersionUID = 1L;

    private final AccountSearchRequest request;

    public AccountSpecification(AccountSearchRequest request) {
        this.request = new AccountSearchRequest(request);
    }

    @Override
    public Predicate toPredicate(Root<Account> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        SearchLogic logic = request.getSearchLogic();
        Predicate p = logic == SearchLogic.AND ? cb.conjunction() : cb.disjunction();
        boolean hasCriteria = false;
        if (request.getName() != null && request.getNameOp() == SearchOperator.EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("name").in(request.getName())) : cb.or(p, root.get("name").in(request.getName()));
            hasCriteria = true;
        }
        if (request.getName() != null && request.getNameOp() == SearchOperator.CONTAINS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("name"), "%" + request.getName() + "%")) : cb.or(p, cb.like(root.get("name"), "%" + request.getName() + "%"));
            hasCriteria = true;
        }
        if (request.getName() != null && request.getNameOp() == SearchOperator.STARTS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("name"), request.getName() + "%")) : cb.or(p, cb.like(root.get("name"), request.getName() + "%"));
            hasCriteria = true;
        }
        if (request.getName() != null && request.getNameOp() == SearchOperator.ENDS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("name"), "%" + request.getName())) : cb.or(p, cb.like(root.get("name"), "%" + request.getName()));
            hasCriteria = true;
        }
        if (request.getName() != null && request.getNameOp() == SearchOperator.IN) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("name").in(request.getName())) : cb.or(p, root.get("name").in(request.getName()));
            hasCriteria = true;
        }
        if (request.getName() != null && request.getNameOp() == SearchOperator.NOT_EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("name").in(request.getName()))) : cb.or(p, cb.not(root.get("name").in(request.getName())));
            hasCriteria = true;
        }
        if (request.getName() != null && request.getNameOp() == SearchOperator.NOT_IN) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("name").in(request.getName()))) : cb.or(p, cb.not(root.get("name").in(request.getName())));
            hasCriteria = true;
        }
        if (request.getName() != null && request.getNameOp() == SearchOperator.REGEX) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("name"), request.getName())) : cb.or(p, cb.like(root.get("name"), request.getName()));
            hasCriteria = true;
        }
        if (request.getTenantId() != null && request.getTenantIdOp() == SearchOperator.EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("tenantId").in(request.getTenantId())) : cb.or(p, root.get("tenantId").in(request.getTenantId()));
            hasCriteria = true;
        }
        if (request.getTenantId() != null && request.getTenantIdOp() == SearchOperator.CONTAINS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("tenantId"), "%" + request.getTenantId() + "%")) : cb.or(p, cb.like(root.get("tenantId"), "%" + request.getTenantId() + "%"));
            hasCriteria = true;
        }
        if (request.getTenantId() != null && request.getTenantIdOp() == SearchOperator.STARTS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("tenantId"), request.getTenantId() + "%")) : cb.or(p, cb.like(root.get("tenantId"), request.getTenantId() + "%"));
            hasCriteria = true;
        }
        if (request.getTenantId() != null && request.getTenantIdOp() == SearchOperator.ENDS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("tenantId"), "%" + request.getTenantId())) : cb.or(p, cb.like(root.get("tenantId"), "%" + request.getTenantId()));
            hasCriteria = true;
        }
        if (request.getTenantId() != null && request.getTenantIdOp() == SearchOperator.IN) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("tenantId").in(request.getTenantId())) : cb.or(p, root.get("tenantId").in(request.getTenantId()));
            hasCriteria = true;
        }
        if (request.getTenantId() != null && request.getTenantIdOp() == SearchOperator.NOT_EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("tenantId").in(request.getTenantId()))) : cb.or(p, cb.not(root.get("tenantId").in(request.getTenantId())));
            hasCriteria = true;
        }
        if (request.getTenantId() != null && request.getTenantIdOp() == SearchOperator.NOT_IN) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("tenantId").in(request.getTenantId()))) : cb.or(p, cb.not(root.get("tenantId").in(request.getTenantId())));
            hasCriteria = true;
        }
        if (request.getTenantId() != null && request.getTenantIdOp() == SearchOperator.REGEX) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("tenantId"), request.getTenantId())) : cb.or(p, cb.like(root.get("tenantId"), request.getTenantId()));
            hasCriteria = true;
        }
        if (request.getCreatedAt() != null && request.getCreatedAtOp() == SearchOperator.AFTER) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.greaterThan(root.get("createdAt"), request.getCreatedAt())) : cb.or(p, cb.greaterThan(root.get("createdAt"), request.getCreatedAt()));
            hasCriteria = true;
        }
        if (!hasCriteria && logic == SearchLogic.OR) {
            return cb.conjunction();
        }
        return p;
    }
}
