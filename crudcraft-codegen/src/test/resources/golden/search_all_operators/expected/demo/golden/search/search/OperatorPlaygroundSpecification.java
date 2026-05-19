package demo.golden.search.search;

import demo.golden.search.OperatorPlayground;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import nl.datasteel.crudcraft.annotations.SearchOperator;
import nl.datasteel.crudcraft.runtime.search.SearchLogic;
import org.springframework.data.jpa.domain.Specification;

/**
 * Generated model file for OperatorPlayground; do not edit manually.
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
 * var request = new OperatorPlaygroundSearchRequest();
 * request.setTitle("CrudCraft");
 * var spec = request.toSpecification();
 *
 * Generation context:
 * - Source model: OperatorPlayground
 * - Package: demo.golden.search.search
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
public class OperatorPlaygroundSpecification implements Specification<OperatorPlayground> {
    private static final long serialVersionUID = 1L;

    private final OperatorPlaygroundSearchRequest request;

    public OperatorPlaygroundSpecification(OperatorPlaygroundSearchRequest request) {
        this.request = new OperatorPlaygroundSearchRequest(request);
    }

    @Override
    public Predicate toPredicate(Root<OperatorPlayground> root, CriteriaQuery<?> query,
            CriteriaBuilder cb) {
        SearchLogic logic = request.getSearchLogic();
        Predicate p = logic == SearchLogic.AND ? cb.conjunction() : cb.disjunction();
        boolean hasCriteria = false;
        if (request.getTitle() != null && request.getTitleOp() == SearchOperator.EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("title").in(request.getTitle())) : cb.or(p, root.get("title").in(request.getTitle()));
            hasCriteria = true;
        }
        if (request.getTitle() != null && request.getTitleOp() == SearchOperator.NOT_EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("title").in(request.getTitle()))) : cb.or(p, cb.not(root.get("title").in(request.getTitle())));
            hasCriteria = true;
        }
        if (request.getTitle() != null && request.getTitleOp() == SearchOperator.CONTAINS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("title"), "%" + request.getTitle() + "%")) : cb.or(p, cb.like(root.get("title"), "%" + request.getTitle() + "%"));
            hasCriteria = true;
        }
        if (request.getTitle() != null && request.getTitleOp() == SearchOperator.STARTS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("title"), request.getTitle() + "%")) : cb.or(p, cb.like(root.get("title"), request.getTitle() + "%"));
            hasCriteria = true;
        }
        if (request.getTitle() != null && request.getTitleOp() == SearchOperator.ENDS_WITH) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("title"), "%" + request.getTitle())) : cb.or(p, cb.like(root.get("title"), "%" + request.getTitle()));
            hasCriteria = true;
        }
        if (request.getTitle() != null && request.getTitleOp() == SearchOperator.REGEX) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.like(root.get("title"), request.getTitle())) : cb.or(p, cb.like(root.get("title"), request.getTitle()));
            hasCriteria = true;
        }
        if (request.getTitle() != null && request.getTitleOp() == SearchOperator.IN) {
            p = logic == SearchLogic.AND ? cb.and(p, root.get("title").in(request.getTitle())) : cb.or(p, root.get("title").in(request.getTitle()));
            hasCriteria = true;
        }
        if (request.getTitle() != null && request.getTitleOp() == SearchOperator.NOT_IN) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.not(root.get("title").in(request.getTitle()))) : cb.or(p, cb.not(root.get("title").in(request.getTitle())));
            hasCriteria = true;
        }
        if (request.getTitleOp() == SearchOperator.IS_EMPTY) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.isEmpty(root.get("title"))) : cb.or(p, cb.isEmpty(root.get("title")));
            hasCriteria = true;
        }
        if (request.getTitleOp() == SearchOperator.NOT_EMPTY) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.isNotEmpty(root.get("title"))) : cb.or(p, cb.isNotEmpty(root.get("title")));
            hasCriteria = true;
        }
        if (request.getScore() != null && request.getScoreOp() == SearchOperator.GT) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.greaterThan(root.get("score"), request.getScore())) : cb.or(p, cb.greaterThan(root.get("score"), request.getScore()));
            hasCriteria = true;
        }
        if (request.getScore() != null && request.getScoreOp() == SearchOperator.GTE) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.greaterThanOrEqualTo(root.get("score"), request.getScore())) : cb.or(p, cb.greaterThanOrEqualTo(root.get("score"), request.getScore()));
            hasCriteria = true;
        }
        if (request.getScore() != null && request.getScoreOp() == SearchOperator.LT) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.lessThan(root.get("score"), request.getScore())) : cb.or(p, cb.lessThan(root.get("score"), request.getScore()));
            hasCriteria = true;
        }
        if (request.getScore() != null && request.getScoreOp() == SearchOperator.LTE) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.lessThanOrEqualTo(root.get("score"), request.getScore())) : cb.or(p, cb.lessThanOrEqualTo(root.get("score"), request.getScore()));
            hasCriteria = true;
        }
        if (request.getScoreStart() != null && request.getScoreEnd() != null && request.getScoreOp() == SearchOperator.RANGE) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.between(root.get("score"), request.getScoreStart(), request.getScoreEnd())) : cb.or(p, cb.between(root.get("score"), request.getScoreStart(), request.getScoreEnd()));
            hasCriteria = true;
        }
        if (request.getScoreStart() != null && request.getScoreEnd() != null && request.getScoreOp() == SearchOperator.BETWEEN) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.between(root.get("score"), request.getScoreStart(), request.getScoreEnd())) : cb.or(p, cb.between(root.get("score"), request.getScoreStart(), request.getScoreEnd()));
            hasCriteria = true;
        }
        if (request.getPublishedAt() != null && request.getPublishedAtOp() == SearchOperator.BEFORE) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.lessThan(root.get("publishedAt"), request.getPublishedAt())) : cb.or(p, cb.lessThan(root.get("publishedAt"), request.getPublishedAt()));
            hasCriteria = true;
        }
        if (request.getPublishedAt() != null && request.getPublishedAtOp() == SearchOperator.AFTER) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.greaterThan(root.get("publishedAt"), request.getPublishedAt())) : cb.or(p, cb.greaterThan(root.get("publishedAt"), request.getPublishedAt()));
            hasCriteria = true;
        }
        if (request.getLabelsOp() == SearchOperator.IS_EMPTY) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.isEmpty(root.get("labels"))) : cb.or(p, cb.isEmpty(root.get("labels")));
            hasCriteria = true;
        }
        if (request.getLabelsOp() == SearchOperator.NOT_EMPTY) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.isNotEmpty(root.get("labels"))) : cb.or(p, cb.isNotEmpty(root.get("labels")));
            hasCriteria = true;
        }
        if (request.getLabelsSize() != null && request.getLabelsSizeOp() == SearchOperator.SIZE_EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.equal(cb.size(root.get("labels")), request.getLabelsSize())) : cb.or(p, cb.equal(cb.size(root.get("labels")), request.getLabelsSize()));
            hasCriteria = true;
        }
        if (request.getLabelsSize() != null && request.getLabelsSizeOp() == SearchOperator.SIZE_GT) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.greaterThan(cb.size(root.get("labels")), request.getLabelsSize())) : cb.or(p, cb.greaterThan(cb.size(root.get("labels")), request.getLabelsSize()));
            hasCriteria = true;
        }
        if (request.getLabelsSize() != null && request.getLabelsSizeOp() == SearchOperator.SIZE_LT) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.lessThan(cb.size(root.get("labels")), request.getLabelsSize())) : cb.or(p, cb.lessThan(cb.size(root.get("labels")), request.getLabelsSize()));
            hasCriteria = true;
        }
        if (request.getLabels() != null && !request.getLabels().isEmpty() && request.getLabelsOp() == SearchOperator.CONTAINS_ALL) {
            Predicate innerPredicate = cb.conjunction();
            for (var item : request.getLabels()) {
                innerPredicate = cb.and(innerPredicate, cb.isMember(item, root.get("labels")));
            }
            p = logic == SearchLogic.AND ? cb.and(p, innerPredicate) : cb.or(p, innerPredicate);
            hasCriteria = true;
        }
        if (request.getAttributes() != null && request.getAttributesOp() == SearchOperator.CONTAINS_KEY) {
            p = logic == SearchLogic.AND ? cb.and(p, root.joinMap("attributes", JoinType.LEFT).key().in(request.getAttributes().keySet())) : cb.or(p, root.joinMap("attributes", JoinType.LEFT).key().in(request.getAttributes().keySet()));
            hasCriteria = true;
        }
        if (request.getAttributes() != null && request.getAttributesOp() == SearchOperator.CONTAINS_VALUE) {
            p = logic == SearchLogic.AND ? cb.and(p, root.joinMap("attributes", JoinType.LEFT).value().in(request.getAttributes().values())) : cb.or(p, root.joinMap("attributes", JoinType.LEFT).value().in(request.getAttributes().values()));
            hasCriteria = true;
        }
        if (request.getAttributesSize() != null && request.getAttributesSizeOp() == SearchOperator.SIZE_EQUALS) {
            p = logic == SearchLogic.AND ? cb.and(p, cb.equal(cb.size(root.get("attributes")), request.getAttributesSize())) : cb.or(p, cb.equal(cb.size(root.get("attributes")), request.getAttributesSize()));
            hasCriteria = true;
        }
        if (!hasCriteria && logic == SearchLogic.OR) {
            return cb.conjunction();
        }
        return p;
    }
}
