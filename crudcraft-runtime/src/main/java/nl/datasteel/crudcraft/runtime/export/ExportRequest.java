/*
 * Copyright (c) 2025 CrudCraft contributors
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Request object for configuring export behavior.
 * Allows controlling which fields and relationships to include in the export.
 */
public class ExportRequest {
    
    /**
     * Fields to include in the export. If null or empty, all DTO fields are included.
     * Use dot notation for nested fields (e.g., "author.name", "category.id").
     */
    private Set<String> includeFields;
    
    /**
     * Fields to exclude from the export. Exclusions take precedence over inclusions.
     * Use dot notation for nested fields (e.g., "author.email", "passwordHash").
     */
    private Set<String> excludeFields;
    
    /**
     * Maximum depth for nested relationships. Default is 1 (only immediate relationships).
     * Set to 0 to exclude all relationships, or higher values for deeper nesting.
     */
    private Integer maxDepth;
    
    /**
     * Whether to include all entity fields (not just DTO fields). Default is false.
     * When true, exports all accessible fields from the entity, subject to security filtering.
     */
    private Boolean includeAllFields;

    public ExportRequest() {
    }

    public ExportRequest(Set<String> includeFields, Set<String> excludeFields, 
                        Integer maxDepth, Boolean includeAllFields) {
        this.includeFields = includeFields;
        this.excludeFields = excludeFields;
        this.maxDepth = maxDepth;
        this.includeAllFields = includeAllFields;
    }

    public Set<String> getIncludeFields() {
        return includeFields == null ? Collections.emptySet() : new HashSet<>(includeFields);
    }

    public void setIncludeFields(Set<String> includeFields) {
        this.includeFields = includeFields;
    }

    public Set<String> getExcludeFields() {
        return excludeFields == null ? Collections.emptySet() : new HashSet<>(excludeFields);
    }

    public void setExcludeFields(Set<String> excludeFields) {
        this.excludeFields = excludeFields;
    }

    public Integer getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {
        this.maxDepth = maxDepth;
    }

    public Boolean getIncludeAllFields() {
        return includeAllFields;
    }

    public void setIncludeAllFields(Boolean includeAllFields) {
        this.includeAllFields = includeAllFields;
    }

    /**
     * Checks if a field should be included in the export based on include/exclude rules.
     *
     * @param fieldPath the field path in dot notation
     * @return true if the field should be included, false otherwise
     */
    public boolean shouldIncludeField(String fieldPath) {
        // Exclusions take precedence
        if (excludeFields != null && excludeFields.contains(fieldPath)) {
            return false;
        }
        
        // If no inclusions specified, include everything (except exclusions)
        if (includeFields == null || includeFields.isEmpty()) {
            return true;
        }
        
        // Check if field or any parent path is in inclusions
        return includeFields.contains(fieldPath) || isParentIncluded(fieldPath);
    }

    /**
     * Checks if any parent path of the given field is included.
     * For example, if "author" is included, then "author.name" should also be included.
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
     * Gets the effective max depth, using a default of 1 if not specified.
     *
     * @return the maximum depth for nested relationships
     */
    public int getEffectiveMaxDepth() {
        return maxDepth != null ? maxDepth : 1;
    }

    /**
     * Gets whether to include all entity fields, using a default of false if not specified.
     *
     * @return true to include all entity fields, false to use only DTO fields
     */
    public boolean isIncludeAllFieldsEnabled() {
        return includeAllFields != null && includeAllFields;
    }
}
