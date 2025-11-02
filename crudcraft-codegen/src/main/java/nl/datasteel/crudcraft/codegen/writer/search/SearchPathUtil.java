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
package nl.datasteel.crudcraft.codegen.writer.search;

/**
 * Utility for converting between dotted paths and DTO properties.
 */
public final class SearchPathUtil {

    /**
     * Private constructor to prevent instantiation.
     * This class provides static utility methods only.
     */
    private SearchPathUtil() {
    }

    /**
     * Converts a dotted path (e.g., "parent.child") to a DTO property name (e.g., "parentChild").
     *
     * @param path the dotted path to convert
     * @return the corresponding DTO property name
     */
    public static String toProperty(String path) {
        String[] parts = path.split("\\.");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(Character.toUpperCase(
                    parts[i].charAt(0)))
                    .append(parts[i]
                            .substring(1));
        }
        return sb.toString();
    }

    /**
     * Builds a CriteriaBuilder path from a dotted property path.
     *
     * @param path the dotted path
     * @return the builder path expression
     */
    public static String buildPath(String path) {
        String[] parts = path.split("\\.");
        StringBuilder sb = new StringBuilder("root");
        for (int i = 0; i < parts.length; i++) {
            if (i < parts.length - 1) {
                sb.append(".join(\"").append(parts[i]).append("\")");
            } else {
                sb.append(".get(\"").append(parts[i]).append("\")");
            }
        }
        return sb.toString();
    }
}

