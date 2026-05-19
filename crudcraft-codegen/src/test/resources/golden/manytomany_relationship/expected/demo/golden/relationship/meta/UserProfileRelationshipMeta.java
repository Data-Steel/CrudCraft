package demo.golden.relationship.meta;

import demo.golden.relationship.UserAccount;
import demo.golden.relationship.UserProfile;
import java.lang.reflect.Field;
import nl.datasteel.crudcraft.runtime.exception.RelationshipException;

/**
 * Generated model file for UserProfile; do not edit manually.
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
 * - Source model: UserProfile
 * - Package: demo.golden.relationship.meta
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
public final class UserProfileRelationshipMeta {
    private static final Field userField;

    private static final Field user_profileField;

    static {
        try {
            userField = UserProfile.class.getDeclaredField("user");
            userField.setAccessible(true);
            user_profileField = UserAccount.class.getDeclaredField("profile");
            user_profileField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RelationshipException("Failed to initialize RelationshipMeta for UserProfile", e);
        }
    }

    private UserProfileRelationshipMeta() {
    }

    public static void fix(UserProfile entity) {
        try {
            @SuppressWarnings("unchecked") UserAccount child = (UserAccount)userField.get(entity);
            if (child != null) {
                user_profileField.set(child, entity);
            }
        } catch (IllegalAccessException e) {
            throw new RelationshipException("Failed to fix relationships for UserProfile", e);
        }
    }

    public static void clear(UserProfile entity) {
        try {
            @SuppressWarnings("unchecked") UserAccount child = (UserAccount)userField.get(entity);
            if (child != null) {
                user_profileField.set(child, null);
            }
        } catch (IllegalAccessException e) {
            throw new RelationshipException("Failed to clear relationships for UserProfile", e);
        }
    }
}
