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
package nl.datasteel.crudcraft.annotations;

/**
 * Enumeration of CRUD (Create, Read, Update, Delete) endpoints.
 */
public enum CrudEndpoint {
    // ── BASIC CRUD ───────────────────────────────
    /**
     * Gets all resources, with pagination and sorting options.
     */
    GET_ALL,
    /**
     * Gets all reference versions of resources, with pagination and sorting options.
     */
    GET_ALL_REF,
    /**
     * Gets a single resource by its ID.
     */
    GET_ONE,
    /**
     * Creates a new resource.
     */
    POST,
    /**
     * Updates an existing resource by its ID.
     */
    PUT,
    /**
     * Partially updates an existing resource by its ID.
     */
    PATCH,
    /**
     * Deletes a resource by its ID.
     */
    DELETE,

    // ── BULK OPERATIONS ─────────────────────────
    /**
     * Creates multiple resources in a single request.
     */
    BULK_CREATE,
    /**
     * Updates multiple resources in a single request.
     */
    BULK_UPDATE,
    /**
     * Partially updates multiple resources in a single request.
     */
    BULK_PATCH,
    /**
     * Creates or updates multiple resources in a single request.
     */
    BULK_UPSERT,
    /**
     * Deletes multiple resources in a single request.
     */
    BULK_DELETE,
    /**
     * Finds multiple resources by their IDs in a single request.
     */
    FIND_BY_IDS,

    // ── META / SUPPORT ──────────────────────────
    /**
     * Checks if a resource exists by its ID.
     */
    EXISTS,
    /**
     * Counts the total number of resources that match the criteria.
     */
    COUNT,
    /**
     * Searches for resources based on specific criteria.
     */
    SEARCH,
    /**
     * Validates a resource without creating or updating it.
     */
    VALIDATE,
    /**
     * Exports resources in a specific format, based on specified criteria.
     */
    EXPORT
}
