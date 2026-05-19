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

package nl.datasteel.crudcraft.runtime.export;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import nl.datasteel.crudcraft.runtime.export.config.ExportProperties;


/**
 * Request object for configuring export behavior. Allows controlling which fields and relationships
 * to include in the export.
 *
 * <p><strong>DTO mode</strong> is the default and is recommended for user-facing exports. It
 * serializes generated response DTOs, applies controller/service security, and keeps memory use
 * predictable for large result sets.
 *
 * <p><strong>Entity mode</strong> serializes JPA entities directly through export metadata. It
 * honors {@code @ExportExclude} and depth limits before querying, but it bypasses DTO-specific
 * redaction and should be reserved for trusted internal/admin exports.
 */
public final class ExportRequest {
    private static final Set<String> SUPPORTED_FORMATS = Set.of("csv", "json", "xlsx");

    /**
     * Fields to include in the export. If null or empty, all DTO fields are included. Use dot
     * notation for nested fields (e.g., "author.name", "category.id").
     */
    private Set<String> includeFields;

    /**
     * Fields to exclude from the export. Exclusions take precedence over inclusions. Use dot
     * notation for nested fields (e.g., "author.email", "passwordHash").
     */
    private Set<String> excludeFields;

    /**
     * Maximum depth for nested relationships. If omitted, runtime-export uses
     * {@code crudcraft.export.max-depth}, which defaults to {@value
     * nl.datasteel.crudcraft.runtime.export.config.ExportProperties#DEFAULT_MAX_DEPTH}. Set to 0
     * to exclude all relationships, or higher values for deeper nesting.
     */
    private Integer maxDepth;

    /** Export mode: DTO-based (default) or entity-based. */
    public enum ExportMode {
        /** Export only DTO fields (default, backward compatible). */
        DTO,
        /** Export entity fields dynamically with full relationship support. */
        ENTITY
    }

    /**
     * The export mode to use. Default is DTO mode for backward compatibility.
     *
     * <ul>
     *   <li><strong>DTO mode</strong>: Exports generated response DTO fields and is safest for
     *       user-facing exports.
     *   <li><strong>ENTITY mode</strong>: Exports any entity field dynamically, with efficient
     *       relationship loading, {@code @ExportExclude} enforcement, and depth validation.
     * </ul>
     *
     * <p>ENTITY mode supports:
     *
     * <ul>
     *   <li>Dynamic field selection from entity at runtime
     *   <li>All relationship types with optimized batch loading
     *   <li>Nested field access via dot notation
     *   <li>@ExportExclude annotation enforcement
     * </ul>
     */
    private ExportMode exportMode;

    /** Creates an empty export request. */
    public ExportRequest() {}

    /**
     * Creates an export request with explicit options.
     *
     * @param includeFields included field paths
     * @param excludeFields excluded field paths
     * @param maxDepth max relationship depth
     * @param exportMode selected export mode
     */
    public ExportRequest(
            @Nullable Set<String> includeFields,
            @Nullable Set<String> excludeFields,
            @Nullable Integer maxDepth,
            @Nullable ExportMode exportMode) {
        this.includeFields = sanitizeFieldPaths(includeFields);
        this.excludeFields = sanitizeFieldPaths(excludeFields);
        this.maxDepth = maxDepth;
        this.exportMode = exportMode;
    }

    /**
     * Returns included field paths.
     *
     * @return included field set
     */
    public @NonNull Set<String> getIncludeFields() {
        return includeFields == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(includeFields);
    }

    /**
     * Sets included field paths.
     *
     * @param includeFields included field set
     */
    public void setIncludeFields(@Nullable Set<String> includeFields) {
        this.includeFields = sanitizeFieldPaths(includeFields);
    }

    /**
     * Returns excluded field paths.
     *
     * @return excluded field set
     */
    public @NonNull Set<String> getExcludeFields() {
        return excludeFields == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(excludeFields);
    }

    /**
     * Sets excluded field paths.
     *
     * @param excludeFields excluded field set
     */
    public void setExcludeFields(@Nullable Set<String> excludeFields) {
        this.excludeFields = sanitizeFieldPaths(excludeFields);
    }

    /**
     * Returns configured max depth.
     *
     * @return max depth
     */
    public @Nullable Integer getMaxDepth() {
        return maxDepth;
    }

    /**
     * Sets max depth.
     *
     * @param maxDepth max depth
     */
    public void setMaxDepth(@Nullable Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Returns configured export mode.
     *
     * @return export mode
     */
    public @Nullable ExportMode getExportMode() {
        return exportMode;
    }

    /**
     * Sets export mode.
     *
     * @param exportMode export mode
     */
    public void setExportMode(@Nullable ExportMode exportMode) {
        this.exportMode = exportMode;
    }

    /**
     * Checks if a field should be included in the export based on include/exclude rules.
     *
     * @param fieldPath the field path in dot notation
     * @return true if the field should be included, false otherwise
     */
    public boolean shouldIncludeField(@Nullable String fieldPath) {
        if (fieldPath == null || fieldPath.isBlank()) {
            return false;
        }
        String normalizedFieldPath = fieldPath.trim();

        // Exclusions take precedence - check both exact match and parent exclusions
        if (excludeFields != null) {
            // Check exact match
            if (excludeFields.contains(normalizedFieldPath)) {
                return false;
            }
            // Check if any parent path is excluded (e.g., "author" excludes "author.name")
            if (isParentExcluded(normalizedFieldPath)) {
                return false;
            }
        }

        // If no inclusions specified, include everything (except exclusions)
        if (includeFields == null || includeFields.isEmpty()) {
            return true;
        }

        // Check if field or any parent path is in inclusions
        return includeFields.contains(normalizedFieldPath) || isParentIncluded(normalizedFieldPath);
    }

    /**
     * Checks if any parent path of the given field is excluded. For example, if "author" is
     * excluded, then "author.name" should also be excluded.
     *
     * @param fieldPath the field path to check
     * @return true if a parent path is excluded
     */
    private boolean isParentExcluded(String fieldPath) {
        if (excludeFields == null) {
            return false;
        }

        String[] parts = fieldPath.split("\\.");
        StringBuilder parentPath = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) {
                parentPath.append(".");
            }
            parentPath.append(parts[i]);

            if (excludeFields.contains(parentPath.toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if any parent path of the given field is included. For example, if "author" is
     * included, then "author.name" should also be included.
     *
     * @param fieldPath the field path to check
     * @return true if a parent path is included
     */
    private boolean isParentIncluded(String fieldPath) {
        if (includeFields == null) {
            return false;
        }

        String[] parts = fieldPath.split("\\.");
        StringBuilder parentPath = new StringBuilder();

        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) {
                parentPath.append(".");
            }
            parentPath.append(parts[i]);

            if (includeFields.contains(parentPath.toString())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if any descendant path of the given field is included. For example, if "author.name"
     * is included, then when checking "author", this will return true because "author" has an
     * included descendant.
     *
     * @param fieldPath the field path to check
     * @return true if any descendant path is included
     */
    public boolean hasIncludedDescendants(@Nullable String fieldPath) {
        if (fieldPath == null
                || fieldPath.isBlank()
                || includeFields == null
                || includeFields.isEmpty()) {
            return false;
        }

        String prefix = fieldPath.trim() + ".";
        for (String includedField : includeFields) {
            if (includedField.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the effective max depth, using {@code crudcraft.export.max-depth}'s default of {@value
     * nl.datasteel.crudcraft.runtime.export.config.ExportProperties#DEFAULT_MAX_DEPTH} when not
     * specified. Negative values are treated as {@code 0} (no relationship nesting).
     *
     * @return the maximum depth for nested relationships (>= 0)
     */
    public int getEffectiveMaxDepth() {
        return getEffectiveMaxDepth(ExportProperties.DEFAULT_MAX_DEPTH);
    }

    /**
     * Gets the effective max depth with the runtime configured default. Negative values are treated
     * as {@code 0} (no relationship nesting).
     *
     * @param configuredDefault default from runtime configuration when request max depth is omitted
     * @return the maximum depth for nested relationships (>= 0)
     */
    public int getEffectiveMaxDepth(int configuredDefault) {
        if (maxDepth == null) {
            return Math.max(0, configuredDefault);
        }
        return Math.max(0, maxDepth);
    }

    /**
     * Gets the effective export mode, using DTO mode as default if not specified.
     *
     * @return the export mode to use
     */
    public @NonNull ExportMode getEffectiveExportMode() {
        return exportMode != null ? exportMode : ExportMode.DTO;
    }

    /**
     * Checks if entity mode is enabled.
     *
     * @return true if entity mode is enabled
     */
    public boolean isEntityModeEnabled() {
        return getEffectiveExportMode() == ExportMode.ENTITY;
    }

    /**
     * Normalizes and validates an export format.
     *
     * @param format requested format; must be one of {@code csv}, {@code json}, or {@code xlsx}
     * @return normalized lowercase format
     * @throws IllegalArgumentException when the format is null, blank, or unsupported
     */
    public static @NonNull String requireSupportedFormat(@Nullable String format) {
        if (format == null) {
            throw new IllegalArgumentException(
                    "Format must not be null; use 'csv', 'json', or 'xlsx'.");
        }
        String normalized = format.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty() || !SUPPORTED_FORMATS.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Unsupported format: " + format + ". Use 'csv', 'json', or 'xlsx'.");
        }
        return normalized;
    }

    private static @Nullable Set<String> sanitizeFieldPaths(@Nullable Set<String> fieldPaths) {
        if (fieldPaths == null) {
            return null;
        }
        Set<String> sanitized = new LinkedHashSet<>();
        for (String path : fieldPaths) {
            if (path == null) {
                continue;
            }
            String trimmed = path.trim();
            if (!trimmed.isEmpty()) {
                requireWellFormedFieldPath(trimmed);
                sanitized.add(trimmed);
            }
        }
        return sanitized;
    }

    private static void requireWellFormedFieldPath(String path) {
        String[] segments = path.split("\\.", -1);
        for (String segment : segments) {
            if (segment.isBlank() || !isJavaIdentifier(segment)) {
                throw new IllegalArgumentException("Malformed export field path: " + path);
            }
        }
    }

    private static boolean isJavaIdentifier(String segment) {
        if (segment.isEmpty() || !Character.isJavaIdentifierStart(segment.charAt(0))) {
            return false;
        }
        for (int i = 1; i < segment.length(); i++) {
            if (!Character.isJavaIdentifierPart(segment.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
