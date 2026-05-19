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

package nl.datasteel.crudcraft.sample.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SecurityConfigTest {

    @Test
    void statelessApiMatcherAllowsBearerRequests() {
        MockHttpServletRequest request = request("/h2-console");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token");

        assertTrue(SecurityConfig.hasBearerToken(request));
        assertTrue(SecurityConfig.isStatelessApiRequest(request));
    }

    @Test
    void statelessApiMatcherAllowsApiRequestsWithoutBearerToken() {
        MockHttpServletRequest request = request("/posts");

        assertFalse(SecurityConfig.hasBearerToken(request));
        assertTrue(SecurityConfig.isStatelessApiRequest(request));
    }

    @Test
    void statelessApiMatcherKeepsBrowserToolingCsrfProtected() {
        assertFalse(SecurityConfig.isStatelessApiRequest(request("/h2-console")));
        assertFalse(SecurityConfig.isStatelessApiRequest(request("/swagger-ui/index.html")));
        assertFalse(SecurityConfig.isStatelessApiRequest(request("/v3/api-docs")));
    }

    @Test
    void bearerTokenMatcherRejectsNonBearerAuthorization() {
        MockHttpServletRequest request = request("/posts");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic token");

        assertFalse(SecurityConfig.hasBearerToken(request));
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setRequestURI(path);
        return request;
    }
}
