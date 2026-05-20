package demo.golden.search.meta;

import demo.golden.search.SearchTag;

/**
 * Generated model file for SearchTag; do not edit manually.
 * @CrudCraft:generated
 *
 * This DTO mirrors the entity fields and annotations exactly. Fields are nullable unless validation annotations such as @NotNull, @NotBlank, or @NotEmpty are present.
 *
 * Included elements:
 * - All entity fields one-to-one
 * - Validation annotations copied verbatim
 * - Nullness and range expectations are expressed through validation annotations and component Javadoc
 *
 * Generation context:
 * - Source model: SearchTag
 * - Package: demo.golden.search.meta
 * - Generator: RelationshipMetaGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
public final class SearchTagRelationshipMeta {
    private SearchTagRelationshipMeta() {
    }

    public static void fix(SearchTag entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity");
        }
        // no bidirectional relationships to fix;
    }

    public static void clear(SearchTag entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity");
        }
        // no bidirectional relationships to clear;
    }
}
