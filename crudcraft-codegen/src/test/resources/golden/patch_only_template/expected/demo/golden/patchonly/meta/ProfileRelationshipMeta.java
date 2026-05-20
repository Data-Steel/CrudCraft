package demo.golden.patchonly.meta;

import demo.golden.patchonly.Profile;

/**
 * Generated model file for Profile; do not edit manually.
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
 * - Source model: Profile
 * - Package: demo.golden.patchonly.meta
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
public final class ProfileRelationshipMeta {
    private ProfileRelationshipMeta() {
    }

    public static void fix(Profile entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity");
        }
        // no bidirectional relationships to fix;
    }

    public static void clear(Profile entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity");
        }
        // no bidirectional relationships to clear;
    }
}
