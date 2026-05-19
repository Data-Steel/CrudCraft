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

package nl.datasteel.crudcraft.runtime.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import nl.datasteel.crudcraft.runtime.exception.BadRequestException;


/** Guard for rejecting search paths that violate generation-bounded constraints. */
public final class SearchPathGuard {
    private static final ConcurrentMap<String, Boolean> MALFORMED_RESULTS =
            new ConcurrentHashMap<>();

    private SearchPathGuard() {}

    /**
     * Validates that a dotted search path does not revisit the same segment.
     *
     * @param path dotted search path
     * @return the original path when valid
     */
    public static String rejectCycles(String path) {
        return requireWellFormed(path);
    }

    /**
     * Validates that a dotted search path does not traverse a metadata-observed cycle.
     *
     * @param path dotted search path
     * @param allowedPaths generated searchable allow-list
     * @return the original path when valid
     */
    public static String rejectCycles(String path, Set<String> allowedPaths) {
        String candidate = requireWellFormed(path);
        if (candidate == null
                || candidate.isBlank()
                || allowedPaths == null
                || allowedPaths.isEmpty()) {
            return candidate;
        }
        String[] segments = candidate.split("\\.");
        if (segments.length < 3) {
            return candidate;
        }
        Map<String, Set<String>> adjacency = adjacencyBySegment(allowedPaths);
        Set<String> visited = new HashSet<>();
        visited.add(segments[0]);
        for (int i = 1; i < segments.length; i++) {
            String previous = segments[i - 1];
            String current = segments[i];
            if (!adjacency.getOrDefault(previous, Set.of()).contains(current)) {
                visited.add(current);
                continue;
            }
            if (!visited.add(current)) {
                throw cycleException(candidate);
            }
        }
        return candidate;
    }

    /**
     * Validates path syntax without making assumptions about entity graph cycles.
     *
     * @param path dotted search path
     * @return original path when valid
     */
    public static String requireWellFormed(String path) {
        if (path == null || path.isBlank()) {
            return path;
        }
        boolean malformed =
                MALFORMED_RESULTS.computeIfAbsent(path, SearchPathGuard::hasBlankSegment);
        if (malformed) {
            throw malformedPathException(path);
        }
        return path;
    }

    private static Map<String, Set<String>> adjacencyBySegment(Set<String> allowedPaths) {
        Map<String, Set<String>> adjacency = new HashMap<>();
        for (String path : allowedPaths) {
            if (path == null || path.isBlank() || hasBlankSegment(path)) {
                continue;
            }
            String[] segments = path.split("\\.");
            for (int i = 1; i < segments.length; i++) {
                adjacency
                        .computeIfAbsent(segments[i - 1], ignored -> new HashSet<>())
                        .add(segments[i]);
            }
        }
        return adjacency;
    }

    private static boolean hasBlankSegment(String path) {
        int segmentStart = 0;
        for (int i = 0; i <= path.length(); i++) {
            if (i != path.length() && path.charAt(i) != '.') {
                continue;
            }
            String segment = path.substring(segmentStart, i);
            if (segment.isBlank()) {
                return true;
            }
            segmentStart = i + 1;
        }
        return false;
    }

    static void clearCycleCacheForTests() {
        MALFORMED_RESULTS.clear();
    }

    private static BadRequestException malformedPathException(String path) {
        return new BadRequestException(
                "Invalid searchable path rejected: "
                        + path
                        + ". Remove empty path segments before retrying.");
    }

    private static BadRequestException cycleException(String path) {
        return new BadRequestException(
                "Search path '"
                        + path
                        + "' revisits a generated relation segment and is rejected to prevent"
                        + " cyclic traversal.");
    }

    /**
     * Rejects dotted search paths whose segment count exceeds the configured maximum depth.
     * Defense-in-depth alongside the generated allow-list: even when an allow-list is empty
     * or bypassed, a crafted deep path cannot drive unbounded join traversal.
     *
     * @param path dotted search path
     * @param maxDepth maximum allowed segment count (paths beyond this are rejected). {@link
     *     Integer#MAX_VALUE} disables the check for request types without generated metadata.
     *     Values of zero or less are rejected as invalid configuration.
     * @return the original path when within the depth budget
     */
    public static String enforceMaxDepth(String path, int maxDepth) {
        if (maxDepth <= 0) {
            throw new BadRequestException(
                    "crudcraft.search.depth must be positive when search path validation runs.");
        }
        if (path == null || path.isBlank() || maxDepth == Integer.MAX_VALUE) {
            return path;
        }
        int segments = 1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '.') {
                segments++;
                if (segments > maxDepth) {
                    throw new BadRequestException(
                            "Search path '"
                                    + path
                                    + "' exceeds the configured maximum depth of "
                                    + maxDepth
                                    + ". Reduce nesting or raise crudcraft.search.depth.");
                }
            }
        }
        return path;
    }
}
