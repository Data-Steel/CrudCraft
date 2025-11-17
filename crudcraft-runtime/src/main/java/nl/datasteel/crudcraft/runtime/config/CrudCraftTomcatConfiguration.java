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
package nl.datasteel.crudcraft.runtime.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Tomcat server to allow square brackets in query parameters.
 * This is necessary for nested search request objects, where Swagger/OpenAPI
 * generates URLs like "author[name]=value" for nested parameters.
 * <p>
 * Note: This configuration only relaxes validation for square brackets, which are
 * required for array and nested object parameters. It does not allow other special
 * characters that could pose security risks.
 */
@Configuration
public class CrudCraftTomcatConfiguration {

    /**
     * Creates a Tomcat connector customizer that allows square brackets in URLs.
     * This is required for search requests with nested relationships, where
     * Swagger/OpenAPI generates URLs like "author[name]=value".
     * <p>
     * Security Note: Only square brackets are allowed. Query parameters are safely
     * bound to SearchRequest DTOs and used in parameterized JPA/QueryDSL queries,
     * preventing injection attacks.
     *
     * @return a TomcatConnectorCustomizer that allows square brackets in query parameters
     */
    @Bean
    public TomcatConnectorCustomizer tomcatConnectorCustomizer() {
        return (Connector connector) -> {
            // Only allow square brackets - the minimum needed for nested search parameters
            connector.setProperty("relaxedQueryChars", "[]");
        };
    }
}
