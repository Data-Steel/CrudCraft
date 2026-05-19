package demo.golden.endpointmatrix.search;

import demo.golden.endpointmatrix.SearchOnlyEvent;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.search.SearchLogic;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generated model file for SearchOnlyEvent; do not edit manually.
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
 * var request = new SearchOnlyEventSearchRequest();
 * request.setTitle("CrudCraft");
 * var spec = request.toSpecification();
 *
 * Generation context:
 * - Source model: SearchOnlyEvent
 * - Package: demo.golden.endpointmatrix.search
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
public class SearchOnlyEventSpecification implements Specification<SearchOnlyEvent> {
    private static final long serialVersionUID = 1L;

    private final SearchOnlyEventSearchRequest request;

    public SearchOnlyEventSpecification(SearchOnlyEventSearchRequest request) {
        this.request = new SearchOnlyEventSearchRequest(request);
    }

    @Override
    public Predicate toPredicate(Root<SearchOnlyEvent> root, CriteriaQuery<?> query,
            CriteriaBuilder cb) {
        SearchLogic logic = request.getSearchLogic();
        Predicate p = logic == SearchLogic.AND ? cb.conjunction() : cb.disjunction();
        boolean hasCriteria = false;
        if (request.getMessage() != null && request.getMessageOp() == SearchOperator.EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("message").in(request.getMessage())) : cb.or(p, root.get("message").in(request.getMessage()));
            hasCriteria = true;
        }
        if (request.getMessage() != null && request.getMessageOp() == SearchOperator.CONTAINS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("message"), "%" + request.getMessage() + "%")) : cb.or(p, cb.like(root.get("message"), "%" + request.getMessage() + "%"));
            hasCriteria = true;
        }
        if (request.getMessage() != null && request.getMessageOp() == SearchOperator.STARTS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("message"), request.getMessage() + "%")) : cb.or(p, cb.like(root.get("message"), request.getMessage() + "%"));
            hasCriteria = true;
        }
        if (request.getMessage() != null && request.getMessageOp() == SearchOperator.ENDS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("message"), "%" + request.getMessage())) : cb.or(p, cb.like(root.get("message"), "%" + request.getMessage()));
            hasCriteria = true;
        }
        if (request.getMessage() != null && request.getMessageOp() == SearchOperator.IN) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("message").in(request.getMessage())) : cb.or(p, root.get("message").in(request.getMessage()));
            hasCriteria = true;
        }
        if (request.getMessage() != null && request.getMessageOp() == SearchOperator.NOT_EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("message").in(request.getMessage()))) : cb.or(p, cb.not(root.get("message").in(request.getMessage())));
            hasCriteria = true;
        }
        if (request.getMessage() != null && request.getMessageOp() == SearchOperator.NOT_IN) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("message").in(request.getMessage()))) : cb.or(p, cb.not(root.get("message").in(request.getMessage())));
            hasCriteria = true;
        }
        if (request.getMessage() != null && request.getMessageOp() == SearchOperator.REGEX) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("message"), request.getMessage())) : cb.or(p, cb.like(root.get("message"), request.getMessage()));
            hasCriteria = true;
        }
        if (!hasCriteria && logic == SearchLogic.OR) {
            return cb.conjunction();
        }
        return p;
    }
}
