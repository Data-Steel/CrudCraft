package demo.golden.relationship.meta;

import demo.golden.relationship.AccessGroup;
import demo.golden.relationship.UserAccount;
import java.lang.reflect.Field;
import java.util.Collection;
import nl.datasteel.crudcraft.runtime.exception.RelationshipException;

/**
 * Generated model file for UserAccount; do not edit manually.
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
 * - Source model: UserAccount
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
public final class UserAccountRelationshipMeta {
    private static final Field groupsField;

    private static final Field groups_userAccountsField;

    static {
        try {
            groupsField = UserAccount.class.getDeclaredField("groups");
            groupsField.setAccessible(true);
            groups_userAccountsField = AccessGroup.class.getDeclaredField("userAccounts");
            groups_userAccountsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RelationshipException("Failed to initialize RelationshipMeta for UserAccount", e);
        }
    }

    private UserAccountRelationshipMeta() {
    }

    public static void fix(UserAccount entity) {
        try {
            @SuppressWarnings("unchecked") Collection<AccessGroup> groupsChildren = (Collection<AccessGroup>)groupsField.get(entity);
            if (groupsChildren != null) {
                for (AccessGroup groupsChild : groupsChildren) {
                    @SuppressWarnings("unchecked") Collection<UserAccount> groupsInv = (Collection<UserAccount>)groups_userAccountsField.get(groupsChild);
                    if (groupsInv != null) {
                        groupsInv.add(entity);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RelationshipException("Failed to fix relationships for UserAccount", e);
        }
    }

    public static void clear(UserAccount entity) {
        try {
            @SuppressWarnings("unchecked") Collection<AccessGroup> groupsChildren = (Collection<AccessGroup>)groupsField.get(entity);
            if (groupsChildren != null) {
                for (AccessGroup groupsChild : groupsChildren) {
                    @SuppressWarnings("unchecked") Collection<UserAccount> groupsInv = (Collection<UserAccount>)groups_userAccountsField.get(groupsChild);
                    if (groupsInv != null) {
                        groupsInv.remove(entity);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RelationshipException("Failed to clear relationships for UserAccount", e);
        }
    }
}
