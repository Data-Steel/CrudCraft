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
 * Configuration for Tomcat server to relax URL validation.
 * This is necessary to allow square brackets and other special characters
 * in query parameters, which are used for nested search request objects.
 */
@Configuration
public class CrudCraftTomcatConfiguration {

    /**
     * Creates a Tomcat connector customizer that allows square brackets and other
     * special characters in URLs. This is required for search requests with nested
     * relationships, where Swagger/OpenAPI generates URLs like "author[name]=value".
     *
     * @return a TomcatConnectorCustomizer that relaxes URL validation
     */
    @Bean
    public TomcatConnectorCustomizer tomcatConnectorCustomizer() {
        return (Connector connector) -> {
            connector.setProperty("relaxedQueryChars", "[]|{}^&#x5c;&#x60;&quot;&lt;&gt;");
        };
    }
}
