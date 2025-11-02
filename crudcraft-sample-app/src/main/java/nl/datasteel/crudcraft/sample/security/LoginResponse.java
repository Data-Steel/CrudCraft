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

package nl.datasteel.crudcraft.sample.security;

/**
 * Response payload returned when authenticating against {@link AuthController}.
 * Contains the issued JWT token and a note explaining its usage and
 * expiration, so demo users can easily test secured endpoints.
 */
public record LoginResponse(
        /** Signed JWT token to include as a Bearer credential. */ String token,
        /** Human-readable explanation of usage and expiry. */ String message) {}
