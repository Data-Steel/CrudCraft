/*
 * Copyright (c) 2026 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.datasteel.crudcraft.runtime.security.row;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import nl.datasteel.crudcraft.annotations.security.RowPredicate;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A row-level security handler that restricts access to entities based on an {@code ownerId} field.
 * Only entities where the owner field matches the current authenticated user's ID are visible or
 * mutable.
 *
 * @param <T> the entity type
 */
public class OwnerBasedRowSecurity<T> implements RowSecurityHandler<T> {

    /** Logger for this class. */
    private static final Logger log = LoggerFactory.getLogger(OwnerBasedRowSecurity.class);

    /**
     * The name of the field that represents the owner of the entity. Defaults to "ownerId" if not
     * specified.
     */
    private final String ownerField;

    /** Supplies the identifier of the current user. */
    private final Supplier<UUID> userIdSupplier;

    /**
     * Creates a handler using {@code ownerId} as the default field name.
     *
     * @param userIdSupplier supplier for the current user id
     */
    public OwnerBasedRowSecurity(Supplier<UUID> userIdSupplier) {
        this("ownerId", userIdSupplier);
    }

    /**
     * Creates a handler using a custom field name.
     *
     * @param ownerField the name of the field representing the owner
     * @param userIdSupplier supplier for the current user id
     */
    public OwnerBasedRowSecurity(String ownerField, Supplier<UUID> userIdSupplier) {
        this.ownerField = Objects.requireNonNull(ownerField, "ownerField must not be null");
        this.userIdSupplier =
                Objects.requireNonNull(userIdSupplier, "userIdSupplier must not be null");
    }

    /**
     * Builds a row-level specification that only allows access to entities where the {@code
     * ownerField} equals the current user ID.
     *
     * @return the row-level security filter
     */
    @Override
    public RowPredicate<T> rowFilter() {
        return (root, query, cb) -> {
            UUID userId = userIdSupplier.get();
            if (userId == null) {
                log.warn("No authenticated user found; denying all rows.");
                return cb.disjunction();
            }
            return cb.equal(root.get(ownerField), userId);
        };
    }

    /**
     * Ensures that the entity's owner matches the current user, or sets it if not yet assigned.
     *
     * @param entity the entity to secure
     * @throws IllegalStateException if reflection fails
     * @throws AccessDeniedException if the current user is not allowed to mutate this entity
     */
    @Override
    public void apply(T entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        UUID userId = userIdSupplier.get();
        if (userId == null) {
            throw new AccessDeniedException("No authenticated user to apply row security.");
        }

        try {
            ensureOwnership(entity, userId);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Failed to apply row security for field '"
                            + ownerField
                            + "' in "
                            + entity.getClass().getSimpleName(),
                    e);
        }
    }

    /**
     * Ensures that the entity's owner field matches the given user ID, or sets it if currently
     * null.
     *
     * @param entity the entity to check or mutate
     * @param userId the current user's ID
     * @throws ReflectiveOperationException if reflection fails
     * @throws AccessDeniedException if the current user is not allowed to mutate this entity
     */
    private void ensureOwnership(T entity, UUID userId) throws ReflectiveOperationException {
        RowSecurityBeanProperty property =
                RowSecurityBeanProperty.forField(entity.getClass(), ownerField);
        if (property.reader() == null || property.writer() == null) {
            throw new IllegalStateException(
                    "No getter/setter found for '" + ownerField + "' in " + entity.getClass());
        }
        try {
            Object currentValue = property.read(entity);
            if (currentValue == null) {
                property.write(entity, userId);
            } else if (!userId.equals(currentValue)) {
                throw new AccessDeniedException("Entity ownership mismatch - access denied.");
            }
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof ReflectiveOperationException reflective) {
                throw reflective;
            }
            throw ex;
        }
    }
}
