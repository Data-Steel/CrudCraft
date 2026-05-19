package demo.golden.umbrella.meta;

import demo.golden.umbrella.Account;
import demo.golden.umbrella.AccountTag;
import java.lang.reflect.Field;
import java.util.Collection;
import nl.datasteel.crudcraft.runtime.exception.RelationshipException;

/**
 * Generated model file for Account; do not edit manually.
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
 * - Source model: Account
 * - Package: demo.golden.umbrella.meta
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
public final class AccountRelationshipMeta {
    private static final Field tagsField;

    private static final Field tags_accountsField;

    static {
        try {
            tagsField = Account.class.getDeclaredField("tags");
            tagsField.setAccessible(true);
            tags_accountsField = AccountTag.class.getDeclaredField("accounts");
            tags_accountsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RelationshipException("Failed to initialize RelationshipMeta for Account", e);
        }
    }

    private AccountRelationshipMeta() {
    }

    public static void fix(Account entity) {
        try {
            @SuppressWarnings("unchecked") Collection<AccountTag> tagsChildren = (Collection<AccountTag>)tagsField.get(entity);
            if (tagsChildren != null) {
                for (AccountTag tagsChild : tagsChildren) {
                    @SuppressWarnings("unchecked") Collection<Account> tagsInv = (Collection<Account>)tags_accountsField.get(tagsChild);
                    if (tagsInv != null) {
                        tagsInv.add(entity);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RelationshipException("Failed to fix relationships for Account", e);
        }
    }

    public static void clear(Account entity) {
        try {
            @SuppressWarnings("unchecked") Collection<AccountTag> tagsChildren = (Collection<AccountTag>)tagsField.get(entity);
            if (tagsChildren != null) {
                for (AccountTag tagsChild : tagsChildren) {
                    @SuppressWarnings("unchecked") Collection<Account> tagsInv = (Collection<Account>)tags_accountsField.get(tagsChild);
                    if (tagsInv != null) {
                        tagsInv.remove(entity);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            throw new RelationshipException("Failed to clear relationships for Account", e);
        }
    }
}
