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

package nl.datasteel.crudcraft.runtime.security;

import com.fasterxml.jackson.annotation.JsonInclude;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = FieldSecurityIntegrationTest.TestApplication.class)
@AutoConfigureMockMvc(addFilters = false)
public class FieldSecurityIntegrationTest {

    private final MockMvc mockMvc;

    @Autowired
    public FieldSecurityIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void httpReadRedactsFieldForUnprivilegedPrincipal() throws Exception {
        String body = documentBody();

        assertTrue(body.contains("\"title\":\"public title\""));
        assertFalse(body.contains("internalNote"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void httpReadKeepsFieldForPrivilegedPrincipal() throws Exception {
        String body = documentBody();

        assertTrue(body.contains("\"title\":\"public title\""));
        assertTrue(body.contains("\"internalNote\":\"admin only\""));
    }

    private String documentBody() throws Exception {
        MvcResult result =
                mockMvc.perform(get("/field-security/document"))
                        .andExpect(status().isOk())
                        .andReturn();
        return result.getResponse().getContentAsString();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(
            exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
    @Import(FieldSecurityController.class)
    public static class TestApplication {
        // Test-only Spring Boot configuration.
    }

    @RestController
    public static class FieldSecurityController {
        @GetMapping("/field-security/document")
        public DocumentDto document() {
            return FieldSecurityUtil.filterRead(new DocumentDto("public title", "admin only"));
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static final class DocumentDto {
        private final String title;

        @FieldSecurity(readRoles = "ADMIN")
        private String internalNote;

        public DocumentDto(String title, String internalNote) {
            this.title = title;
            this.internalNote = internalNote;
        }

        public String getTitle() {
            return title;
        }

        public String getInternalNote() {
            return internalNote;
        }
    }
}
