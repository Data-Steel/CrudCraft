package demo.golden.relationship.meta;

import demo.golden.relationship.AccessGroup;
import demo.golden.relationship.UserAccount;
import java.lang.reflect.Field;
import java.util.Collection;
import nl.datasteel.crudcraft.runtime.exception.RelationshipException;

/**
 * Generated model file for AccessGroup; do not edit manually.
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
 * - Source model: AccessGroup
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
public final class AccessGroupRelationshipMeta {
    private static final Field usersField;

    private static final Field users_groupsField;

    static {
        try {
            usersField = AccessGroup.class.getDeclaredField("users");
            usersField.setAccessible(true);
            users_groupsField = UserAccount.class.getDeclaredField("groups");
            users_groupsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RelationshipException("Failed to initialize RelationshipMeta for AccessGroup", e);
        }
    }

    private AccessGroupRelationshipMeta() {
    }

    public static void fix(AccessGroup entity) {
        try {
            @SuppressWarnings("unchecked") Collection<UserAccount> usersChildren = (Collection<UserAccount>)usersField.get(entity);
            if (usersChildren != null) {
                for (UserAccount usersChild : usersChildren) {
                    @SuppressWarnings("unchecked") Collection<AccessGroup> usersInv = (Collection<AccessGroup>)users_groupsField.get(usersChild);
                    if (usersInv != null) {
                        usersInv.add(entity);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RelationshipException("Failed to fix relationships for AccessGroup", e);
        }
    }

    public static void clear(AccessGroup entity) {
        try {
            @SuppressWarnings("unchecked") Collection<UserAccount> usersChildren = (Collection<UserAccount>)usersField.get(entity);
            if (usersChildren != null) {
                for (UserAccount usersChild : usersChildren) {
                    @SuppressWarnings("unchecked") Collection<AccessGroup> usersInv = (Collection<AccessGroup>)users_groupsField.get(usersChild);
                    if (usersInv != null) {
                        usersInv.remove(entity);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RelationshipException("Failed to clear relationships for AccessGroup", e);
        }
    }
}
