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
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for CrudCraftTomcatConfiguration.
 */
class CrudCraftTomcatConfigurationTest {

    @Test
    void tomcatConnectorCustomizerBeanIsCreated() {
        CrudCraftTomcatConfiguration config = new CrudCraftTomcatConfiguration();
        TomcatConnectorCustomizer customizer = config.tomcatConnectorCustomizer();
        assertNotNull(customizer, "TomcatConnectorCustomizer bean should not be null");
    }

    @Test
    void tomcatConnectorCustomizerSetsRelaxedQueryChars() {
        CrudCraftTomcatConfiguration config = new CrudCraftTomcatConfiguration();
        TomcatConnectorCustomizer customizer = config.tomcatConnectorCustomizer();
        
        // Create a test connector
        Connector connector = new Connector();
        
        // Apply the customizer
        customizer.customize(connector);
        
        // Verify the relaxedQueryChars property is set
        String relaxedQueryChars = (String) connector.getProperty("relaxedQueryChars");
        assertNotNull(relaxedQueryChars, "relaxedQueryChars property should be set");
        assertEquals("[]", relaxedQueryChars, 
                "relaxedQueryChars should only allow square brackets for security");
    }
}
