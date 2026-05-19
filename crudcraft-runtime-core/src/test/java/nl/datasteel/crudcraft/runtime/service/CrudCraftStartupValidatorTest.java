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

package nl.datasteel.crudcraft.runtime.service;

import java.util.Map;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.mapper.EntityMapper;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class CrudCraftStartupValidatorTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void validatesGeneratedServiceCollaborators() {
        ApplicationContext context = mock(ApplicationContext.class);
        TestService service =
                new TestService(
                        (JpaRepository<TestEntity, UUID>)
                                mock(
                                        JpaRepository.class,
                                        org.mockito.Mockito.withSettings()
                                                .extraInterfaces(JpaSpecificationExecutor.class)),
                        new TestMapper(),
                        TestEntity.class,
                        TestResponse.class,
                        TestRef.class);
        when(context.getBeansOfType(CrudService.class, false, false))
                .thenReturn(Map.of("testService", service));

        CrudCraftStartupValidator validator = new CrudCraftStartupValidator(context);

        assertDoesNotThrow(validator::validate);
        assertDoesNotThrow(validator::afterSingletonsInstantiated);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void validatesAopProxiedGeneratedServiceTarget() {
        ApplicationContext context = mock(ApplicationContext.class);
        TestService service =
                new TestService(
                        (JpaRepository<TestEntity, UUID>)
                                mock(
                                        JpaRepository.class,
                                        org.mockito.Mockito.withSettings()
                                                .extraInterfaces(JpaSpecificationExecutor.class)),
                        new TestMapper(),
                        TestEntity.class,
                        TestResponse.class,
                        TestRef.class);
        ProxyFactory proxyFactory = new ProxyFactory(service);
        proxyFactory.setProxyTargetClass(true);
        CrudService proxy = (CrudService) proxyFactory.getProxy();
        when(context.getBeansOfType(CrudService.class, false, false))
                .thenReturn(Map.of("testService", proxy));

        CrudCraftStartupValidator validator = new CrudCraftStartupValidator(context);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void failsFastWhenGeneratedServiceMissesRequiredCollaborators() {
        ApplicationContext context = mock(ApplicationContext.class);
        TestService service = new TestService(null, null, null, null, null);
        when(context.getBeansOfType(CrudService.class, false, false))
                .thenReturn(Map.of("brokenService", service));

        CrudCraftStartupValidator validator = new CrudCraftStartupValidator(context);

        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(ex.getMessage().contains("brokenService"));
        assertTrue(ex.getMessage().contains("repository"));
        assertTrue(ex.getMessage().contains("mapper"));
        assertTrue(ex.getMessage().contains("entityClass"));
    }

    private static final class TestEntity {
        private UUID id;
    }

    private record TestRequest(UUID id) {}

    private record TestResponse(UUID id) {}

    private record TestRef(UUID id) {}

    private static final class TestMapper
            implements EntityMapper<TestEntity, TestRequest, TestResponse, TestRef, UUID> {
        @Override
        public TestEntity fromRequest(TestRequest request) {
            TestEntity entity = new TestEntity();
            entity.id = request.id();
            return entity;
        }

        @Override
        public TestEntity update(TestEntity entity, TestRequest request) {
            entity.id = request.id();
            return entity;
        }

        @Override
        public TestEntity patch(TestEntity entity, TestRequest request) {
            entity.id = request.id();
            return entity;
        }

        @Override
        public TestResponse toResponse(TestEntity entity) {
            return new TestResponse(entity.id);
        }

        @Override
        public TestRef toRef(TestEntity entity) {
            return new TestRef(entity.id);
        }

        @Override
        public UUID getIdFromRequest(TestRequest request) {
            return request.id();
        }
    }

    private static class TestService
            extends AbstractCrudService<TestEntity, TestRequest, TestResponse, TestRef, UUID> {
        protected TestService(
                JpaRepository<TestEntity, UUID> repository,
                EntityMapper<TestEntity, TestRequest, TestResponse, TestRef, UUID> mapper,
                Class<TestEntity> entityClass,
                Class<TestResponse> responseClass,
                Class<TestRef> refClass) {
            super(repository, mapper, entityClass, responseClass, refClass);
        }
    }
}
