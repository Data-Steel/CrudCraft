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

package nl.datasteel.crudcraft.starter.security;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import nl.datasteel.crudcraft.annotations.security.FieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.controller.response.ErrorResponse;
import nl.datasteel.crudcraft.runtime.security.DefaultFieldSecurityAdapter;
import nl.datasteel.crudcraft.runtime.security.FieldSecurityRuntimeExtension;
import nl.datasteel.crudcraft.runtime.security.scope.PrincipalScopeAccessor;
import nl.datasteel.crudcraft.runtime.security.scope.SpringSecurityPrincipalScopeAccessor;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class CrudCraftSecurityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(CrudCraftSecurityAutoConfiguration.class));

    @Test
    void registersDefaultSecurityBeans() {
        contextRunner.run(
                context -> {
                    assertInstanceOf(
                            DefaultFieldSecurityAdapter.class,
                            context.getBean(FieldSecurityAdapter.class));
                    assertInstanceOf(
                            SpringSecurityPrincipalScopeAccessor.class,
                            context.getBean(PrincipalScopeAccessor.class));
                    assertInstanceOf(
                            FieldSecurityRuntimeExtension.class,
                            context.getBean(CrudRuntimeExtension.class));
                    assertInstanceOf(
                            CrudCraftSecurityExceptionHandler.class,
                            context.getBean(CrudCraftSecurityExceptionHandler.class));
                });
    }

    @Test
    void keepsUserProvidedFieldSecurityAdapter() {
        contextRunner
                .withUserConfiguration(CustomFieldSecurityAdapterConfiguration.class)
                .run(
                        context -> {
                            assertInstanceOf(
                                    CustomFieldSecurityAdapter.class,
                                    context.getBean(FieldSecurityAdapter.class));
                            assertInstanceOf(
                                    FieldSecurityRuntimeExtension.class,
                                    context.getBean(CrudRuntimeExtension.class));
                        });
    }

    @Test
    void keepsUserProvidedPrincipalScopeAccessor() {
        contextRunner
                .withUserConfiguration(CustomPrincipalScopeAccessorConfiguration.class)
                .run(
                        context ->
                                assertInstanceOf(
                                        CustomPrincipalScopeAccessor.class,
                                        context.getBean(PrincipalScopeAccessor.class)));
    }

    @Test
    void keepsUserProvidedRuntimeExtensionBean() {
        contextRunner
                .withUserConfiguration(CustomRuntimeExtensionConfiguration.class)
                .run(
                        context -> {
                            assertInstanceOf(
                                    CustomRuntimeExtension.class,
                                    context.getBean("crudCraftFieldSecurityRuntimeExtension"));
                            assertInstanceOf(
                                    DefaultFieldSecurityAdapter.class,
                                    context.getBean(FieldSecurityAdapter.class));
                            assertInstanceOf(
                                    SpringSecurityPrincipalScopeAccessor.class,
                                    context.getBean(PrincipalScopeAccessor.class));
                        });
    }

    @Test
    void runtimeExtensionFactoryRejectsNullAdapter() {
        CrudCraftSecurityAutoConfiguration configuration = new CrudCraftSecurityAutoConfiguration();
        assertThrows(
                IllegalArgumentException.class,
                () -> configuration.crudCraftFieldSecurityRuntimeExtension(null));
    }

    @Test
    void securityExceptionHandlerMapsSpringAccessDeniedToForbidden() {
        Instant fixed = Instant.parse("2026-05-14T10:15:30Z");
        CrudCraftSecurityExceptionHandler handler =
                new CrudCraftSecurityExceptionHandler(Clock.fixed(fixed, ZoneOffset.UTC));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secured");

        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(403, response.getBody().status());
        assertEquals("denied", response.getBody().message());
        assertEquals("/api/secured", response.getBody().path());
        assertEquals(fixed, response.getBody().timestamp());
    }

    @Test
    void securityExceptionHandlerUsesFallbackMessageForBlankAccessDeniedMessage() {
        Instant fixed = Instant.parse("2026-05-14T10:15:30Z");
        CrudCraftSecurityExceptionHandler handler =
                new CrudCraftSecurityExceptionHandler(Clock.fixed(fixed, ZoneOffset.UTC));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secured");

        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(new AccessDeniedException(" "), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access is denied.", response.getBody().message());
        assertEquals("/api/secured", response.getBody().path());
        assertEquals(fixed, response.getBody().timestamp());
    }

    @Test
    void securityExceptionHandlerUsesFallbackMessageForNullAccessDeniedMessage() {
        Instant fixed = Instant.parse("2026-05-14T10:15:30Z");
        CrudCraftSecurityExceptionHandler handler =
                new CrudCraftSecurityExceptionHandler(Clock.fixed(fixed, ZoneOffset.UTC));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/secured");

        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(new AccessDeniedException((String) null), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access is denied.", response.getBody().message());
        assertEquals("/api/secured", response.getBody().path());
        assertEquals(fixed, response.getBody().timestamp());
    }

    @Test
    void runtimeExtensionDelegatesReadAndWriteCallsToConfiguredAdapter() {
        contextRunner
                .withUserConfiguration(TransformingFieldSecurityAdapterConfiguration.class)
                .run(
                        context -> {
                            @SuppressWarnings("unchecked")
                            CrudRuntimeExtension<Object, Object> extension =
                                    (CrudRuntimeExtension<Object, Object>)
                                            context.getBean(CrudRuntimeExtension.class);

                            assertEquals("read:payload", extension.afterRead("payload"));
                            assertEquals("write:payload:null", extension.beforeCreate("payload"));
                            assertEquals(
                                    "write:payload:existing",
                                    extension.beforeUpdate("payload", "existing"));
                            assertNull(extension.readFilter(Object.class));
                        });
    }

    @Configuration(proxyBeanMethods = false)
    public static class CustomFieldSecurityAdapterConfiguration {

        @Bean
        public FieldSecurityAdapter customFieldSecurityAdapter() {
            return new CustomFieldSecurityAdapter();
        }
    }

    public static class CustomFieldSecurityAdapter implements FieldSecurityAdapter {}

    @Configuration(proxyBeanMethods = false)
    public static class CustomPrincipalScopeAccessorConfiguration {

        @Bean
        public PrincipalScopeAccessor customPrincipalScopeAccessor() {
            return new CustomPrincipalScopeAccessor();
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class CustomRuntimeExtensionConfiguration {

        @Bean("crudCraftFieldSecurityRuntimeExtension")
        public CrudRuntimeExtension<?, ?> customRuntimeExtension() {
            return new CustomRuntimeExtension();
        }
    }

    @Configuration(proxyBeanMethods = false)
    public static class TransformingFieldSecurityAdapterConfiguration {

        @Bean
        public FieldSecurityAdapter customFieldSecurityAdapter() {
            return new TransformingFieldSecurityAdapter();
        }
    }

    public static class CustomPrincipalScopeAccessor implements PrincipalScopeAccessor {

        @Override
        public Optional<Object> claim(String claimName) {
            return Optional.empty();
        }

        @Override
        public Set<String> roles() {
            return Set.of();
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }
    }

    public static class CustomRuntimeExtension implements CrudRuntimeExtension<Object, Object> {}

    public static class TransformingFieldSecurityAdapter implements FieldSecurityAdapter {

        @Override
        public <T> T filterRead(T dto) {
            @SuppressWarnings("unchecked")
            T transformed = (T) ("read:" + dto);
            return transformed;
        }

        @Override
        public <T> T filterWrite(T request, Object existing) {
            @SuppressWarnings("unchecked")
            T transformed = (T) ("write:" + request + ":" + existing);
            return transformed;
        }
    }
}
