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

package nl.datasteel.crudcraft.runtime.service;

import edu.umd.cs.findbugs.annotations.Nullable;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import java.util.ArrayList;
import java.util.List;
import nl.datasteel.crudcraft.runtime.InternalApi;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;


/**
 * Keyset pagination layer: cursor validation, stable sort construction, cursor predicates, and
 * boundary cursor extraction.
 *
 * @param <T> entity type
 * @param <U> request DTO type
 * @param <R> full response DTO type
 * @param <F> reference DTO type
 * @param <ID> identifier type
 */
@InternalApi
abstract class KeysetPaginationService<T, U, R, F, ID>
        extends ProjectionService<T, U, R, F, ID> {

    /**
     * Retrieves a single keyset page for the provided projection.
     *
     * <p>The returned cursor is an opaque token. Clients should replay it unchanged with the same
     * sort order to obtain the next page.
     *
     * @param specification optional filter specification
     * @param limit maximum number of rows to return
     * @param cursor optional cursor returned by the previous keyset response
     * @param sort sort order used for the keyset window
     * @param projection projection DTO type
     * @param <P> projection response type
     * @return keyset page with rows and optional next cursor
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <P> KeysetPage<P> findAllKeyset(
            Specification<T> specification,
            int limit,
            String cursor,
            Sort sort,
            Class<P> projection) {
        validateKeysetLimit(limit);
        Sort.Order order = requireSingleSortOrder(sort);
        validateSort(sort);

        Specification<T> cursorSpec = buildKeysetCursorSpec(cursor, order);
        Specification<T> spec = combine(combine(specification, runtimeReadFilter()), cursorSpec);
        Sort stableSort = stableSort(order);
        Pageable pageable = keysetPageable(limit, stableSort);
        Class<P> effectiveProjection = projection != null ? projection : (Class<P>) responseClass();

        Page<P> page = findKeysetPage(spec, pageable, effectiveProjection);
        List<P> rawRows = new ArrayList<>(page.getContent());
        boolean hasNext = trimToLimit(rawRows, limit);
        List<P> rows = rawRows.stream().map(this::afterRead).toList();
        if (!hasNext) {
            return new KeysetPage<>(rows, null);
        }

        String nextCursor = resolveNextCursor(rawRows, order);
        return new KeysetPage<>(rows, nextCursor);
    }

    private void validateKeysetLimit(int limit) {
        if (limit <= 0) {
            throw new BadRequestException("Keyset pagination requires a positive limit");
        }
    }

    private Sort.Order requireSingleSortOrder(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            throw new BadRequestException("Keyset pagination requires deterministic sorting");
        }
        if (sort.stream().count() != 1) {
            throw new BadRequestException("Keyset pagination supports exactly one sort field");
        }
        return sort.iterator().next();
    }

    private Sort stableSort(Sort.Order order) {
        return Sort.by(order).and(Sort.by(order.getDirection(), resolveIdAttributeName()));
    }

    private Pageable keysetPageable(int limit, Sort stableSort) {
        return PageRequest.of(0, limit + 1, stableSort);
    }

    private <P> Page<P> findKeysetPage(
            Specification<T> spec, Pageable pageable, Class<P> effectiveProjection) {
        if (isDefaultProjection(effectiveProjection)) {
            return mapDefaultProjectionPage(spec, pageable, effectiveProjection);
        }
        Page<P> page = projectPage(spec, pageable, effectiveProjection);
        if (page != null) {
            return page;
        }
        return queryExecutor().findAll(spec, pageable, effectiveProjection);
    }

    private <P> boolean trimToLimit(List<P> rows, int limit) {
        boolean hasNext = rows.size() > limit;
        if (hasNext) {
            rows.subList(limit, rows.size()).clear();
        }
        return hasNext;
    }

    private String resolveNextCursor(List<?> rawRows, Sort.Order order) {
        String projectionCursor = tryBuildCursorFromProjection(rawRows, order);
        if (projectionCursor != null) {
            return projectionCursor;
        }
        throw new BadRequestException(
                "Keyset pagination projection must expose sort field '"
                        + order.getProperty()
                        + "' and identifier field '"
                        + resolveIdAttributeName()
                        + "'.");
    }

    private Specification<T> buildKeysetCursorSpec(String cursor, Sort.Order order) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        KeysetCursorCodec.CursorData cursorData = KeysetCursorCodec.decode(cursor);
        if (!order.getProperty().equals(cursorData.property())
                || !order.getDirection().name().equals(cursorData.direction())) {
            throw new BadRequestException("Cursor does not match requested sort");
        }
        return (root, query, cb) -> {
            final Object sortValue;
            final Object idValue;
            try {
                sortValue =
                        KeysetCursorCodec.parseValue(
                                KeysetCursorCodec.resolveJavaType(root, order.getProperty()),
                                cursorData.sortValue());
                idValue =
                        KeysetCursorCodec.parseValue(
                                KeysetCursorCodec.resolveJavaType(root, resolveIdAttributeName()),
                                cursorData.idValue());
            } catch (RuntimeException ex) {
                throw new BadRequestException(
                        "Invalid keyset cursor value. Use the opaque cursor returned by the"
                                + " previous response with the same sort.");
            }
            jakarta.persistence.criteria.Path<Comparable<Object>> sortPath =
                    KeysetCursorCodec.resolvePath(root, order.getProperty());
            jakarta.persistence.criteria.Path<Comparable<Object>> idPath =
                    KeysetCursorCodec.resolvePath(root, resolveIdAttributeName());
            @SuppressWarnings("unchecked")
            Comparable<Object> comparableSortValue = (Comparable<Object>) sortValue;
            @SuppressWarnings("unchecked")
            Comparable<Object> comparableIdValue = (Comparable<Object>) idValue;
            boolean ascending = order.getDirection().isAscending();
            var primary =
                    ascending
                            ? cb.greaterThan(sortPath, comparableSortValue)
                            : cb.lessThan(sortPath, comparableSortValue);
            var tie =
                    ascending
                            ? cb.greaterThan(idPath, comparableIdValue)
                            : cb.lessThan(idPath, comparableIdValue);
            return cb.or(primary, cb.and(cb.equal(sortPath, sortValue), tie));
        };
    }

    private String tryBuildCursorFromProjection(List<?> rows, Sort.Order order) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        Object boundary = rows.get(rows.size() - 1);
        try {
            Object sortValue = KeysetCursorCodec.extractPathValue(boundary, order.getProperty());
            Object idValue = KeysetCursorCodec.extractPathValue(boundary, resolveIdAttributeName());
            if (sortValue == null || idValue == null) {
                return null;
            }
            return KeysetCursorCodec.encode(
                    order.getProperty(), order.getDirection().name(), sortValue, idValue);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private void validateSort(Sort sort) {
        if (sort == null || sort.isUnsorted() || metamodel() == null) {
            return;
        }
        for (Sort.Order order : sort) {
            validateSortProperty(order.getProperty());
        }
    }

    private void validateSortProperty(String property) {
        String[] parts = property.split("\\.");
        ManagedType<?> type = metamodel().managedType(entityClass());
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            Attribute<?, ?> attribute;
            try {
                attribute = type.getAttribute(part);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Unsupported sort field: " + property);
            }
            if (i == parts.length - 1) {
                return;
            }
            if (attribute.isCollection()) {
                throw new BadRequestException("Unsupported nested sort field: " + property);
            }
            try {
                type = metamodel().managedType(attribute.getJavaType());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Unsupported nested sort field: " + property);
            }
        }
    }

    protected abstract @Nullable Metamodel metamodel();
}
