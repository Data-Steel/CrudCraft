package demo.golden.omit.meta;

import demo.golden.omit.Article;

/**
 * Generated model file for Article; do not edit manually.
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
 * - Source model: Article
 * - Package: demo.golden.omit.meta
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
public final class ArticleRelationshipMeta {
    private ArticleRelationshipMeta() {
    }

    public static void fix(Article entity) {
        entity.getClass();
        // no bidirectional relationships to fix;
    }

    public static void clear(Article entity) {
        entity.getClass();
        // no bidirectional relationships to clear;
    }
}
