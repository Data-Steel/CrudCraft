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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import nl.datasteel.crudcraft.runtime.service.projection.ProjectionAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class ServiceCollaboratorsTest {

    @Test
    void returnsEmptyExtensionsAndNullProjectionWithoutContext() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();

        assertTrue(collaborators.contextExtensions().isEmpty());
        assertTrue(collaborators.contextExtensions().isEmpty());
        assertNull(collaborators.projectionAdapter());
        assertNull(collaborators.projectionAdapter());
    }

    @Test
    void resolvesAndCachesContextCollaborators() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext context = mock(ApplicationContext.class);
        CrudRuntimeExtension<?, ?> extension = mock(CrudRuntimeExtension.class);
        ProjectionAdapter projectionAdapter = mock(ProjectionAdapter.class);
        when(context.getBeansOfType(CrudRuntimeExtension.class))
                .thenReturn(Map.of("extension", extension));
        when(context.getBean(ProjectionAdapter.class)).thenReturn(projectionAdapter);

        collaborators.setApplicationContext(context);
        List<CrudRuntimeExtension<Object, Object>> extensions = collaborators.contextExtensions();

        assertEquals(List.of(extension), extensions);
        assertSame(extensions, collaborators.contextExtensions());
        assertSame(projectionAdapter, collaborators.projectionAdapter());
        assertSame(projectionAdapter, collaborators.projectionAdapter());
        verify(context, times(1)).getBeansOfType(CrudRuntimeExtension.class);
        verify(context, times(1)).getBean(ProjectionAdapter.class);
    }

    @Test
    void resetContextClearsCachedCollaborators() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext firstContext = mock(ApplicationContext.class);
        ApplicationContext secondContext = mock(ApplicationContext.class);
        CrudRuntimeExtension<?, ?> extension = mock(CrudRuntimeExtension.class);
        ProjectionAdapter projectionAdapter = mock(ProjectionAdapter.class);
        when(firstContext.getBeansOfType(CrudRuntimeExtension.class)).thenReturn(Map.of());
        when(firstContext.getBean(ProjectionAdapter.class)).thenThrow(mock(BeansException.class));
        when(secondContext.getBeansOfType(CrudRuntimeExtension.class))
                .thenReturn(Map.of("extension", extension));
        when(secondContext.getBean(ProjectionAdapter.class)).thenReturn(projectionAdapter);

        collaborators.setApplicationContext(firstContext);
        assertTrue(collaborators.contextExtensions().isEmpty());
        assertNull(collaborators.projectionAdapter());

        collaborators.setApplicationContext(secondContext);

        assertEquals(List.of(extension), collaborators.contextExtensions());
        assertSame(projectionAdapter, collaborators.projectionAdapter());
    }

    @Test
    void projectionSupportIsCachedAndResetWhenContextChanges() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext firstContext = mock(ApplicationContext.class);
        ApplicationContext secondContext = mock(ApplicationContext.class);
        ProjectionAdapter firstAdapter = mock(ProjectionAdapter.class);
        ProjectionAdapter secondAdapter = mock(ProjectionAdapter.class);

        when(firstContext.getBean(ProjectionAdapter.class)).thenReturn(firstAdapter);
        when(firstAdapter.supports(String.class)).thenReturn(true);
        when(secondContext.getBean(ProjectionAdapter.class)).thenReturn(secondAdapter);
        when(secondAdapter.supports(String.class)).thenReturn(false);

        collaborators.setApplicationContext(firstContext);
        assertTrue(collaborators.supportsProjection(String.class));
        assertTrue(collaborators.supportsProjection(String.class));
        verify(firstAdapter, times(1)).supports(String.class);

        collaborators.setApplicationContext(secondContext);
        assertFalse(collaborators.supportsProjection(String.class));
        verify(secondAdapter, times(1)).supports(String.class);
        verify(secondAdapter, never()).supports(Integer.class);
    }

    @Test
    void transactionManagerIsNullWithoutContextAndResultIsCached() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();

        assertNull(collaborators.transactionManager());
        assertNull(collaborators.transactionManager());
    }

    @Test
    void supportsProjectionReturnsFalseWhenProjectionTypeIsNull() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();

        assertFalse(collaborators.supportsProjection(null));
    }

    @Test
    void supportsProjectionReturnsFalseWhenProjectionAdapterIsUnavailable() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();

        assertFalse(collaborators.supportsProjection(String.class));
    }

    @Test
    void transactionManagerResolvesAndCachesWhenContextProvidesBean() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext context = mock(ApplicationContext.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        when(context.getBean(PlatformTransactionManager.class)).thenReturn(transactionManager);

        collaborators.setApplicationContext(context);

        assertSame(transactionManager, collaborators.transactionManager());
        assertSame(transactionManager, collaborators.transactionManager());
        verify(context, times(1)).getBean(PlatformTransactionManager.class);
    }

    @Test
    void transactionManagerMissingBeanIsNullAndCanRecoverAfterContextReset() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext firstContext = mock(ApplicationContext.class);
        ApplicationContext secondContext = mock(ApplicationContext.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);

        when(firstContext.getBean(PlatformTransactionManager.class))
                .thenThrow(mock(BeansException.class));
        when(secondContext.getBean(PlatformTransactionManager.class)).thenReturn(transactionManager);

        collaborators.setApplicationContext(firstContext);
        assertNull(collaborators.transactionManager());

        collaborators.setApplicationContext(secondContext);
        assertSame(transactionManager, collaborators.transactionManager());
    }

    @Test
    void readDeniedAuditHooksResolveAndCache() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext context = mock(ApplicationContext.class);
        ReadDeniedAuditHook hook = mock(ReadDeniedAuditHook.class);
        when(context.getBeansOfType(ReadDeniedAuditHook.class)).thenReturn(Map.of("hook", hook));

        collaborators.setApplicationContext(context);

        assertEquals(List.of(hook), collaborators.readDeniedAuditHooks());
        assertSame(collaborators.readDeniedAuditHooks(), collaborators.readDeniedAuditHooks());
        verify(context, times(1)).getBeansOfType(ReadDeniedAuditHook.class);
    }

    @Test
    void readDeniedAuditHooksResetWithNewContext() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext firstContext = mock(ApplicationContext.class);
        ApplicationContext secondContext = mock(ApplicationContext.class);
        ReadDeniedAuditHook hook = mock(ReadDeniedAuditHook.class);
        when(firstContext.getBeansOfType(ReadDeniedAuditHook.class)).thenReturn(Map.of());
        when(secondContext.getBeansOfType(ReadDeniedAuditHook.class)).thenReturn(Map.of("hook", hook));

        collaborators.setApplicationContext(firstContext);
        assertTrue(collaborators.readDeniedAuditHooks().isEmpty());

        collaborators.setApplicationContext(secondContext);
        assertEquals(List.of(hook), collaborators.readDeniedAuditHooks());
    }

    @Test
    void readDeniedAuditHooksWithoutContextReturnsCachedEmpty() {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();

        assertTrue(collaborators.readDeniedAuditHooks().isEmpty());
        assertTrue(collaborators.readDeniedAuditHooks().isEmpty());
    }

    @Test
    void contextExtensionsConcurrentInitializationCoversInnerFastPath() throws Exception {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext context = mock(ApplicationContext.class);
        CrudRuntimeExtension<?, ?> extension = mock(CrudRuntimeExtension.class);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        when(context.getBeansOfType(CrudRuntimeExtension.class))
                .thenAnswer(
                        invocation -> {
                            entered.countDown();
                            release.await();
                            return Map.of("extension", extension);
                        });

        collaborators.setApplicationContext(context);
        AtomicReference<List<CrudRuntimeExtension<Object, Object>>> first = new AtomicReference<>();
        AtomicReference<List<CrudRuntimeExtension<Object, Object>>> second = new AtomicReference<>();
        Thread firstThread = new Thread(() -> first.set(collaborators.contextExtensions()));
        Thread secondThread = new Thread(() -> second.set(collaborators.contextExtensions()));

        firstThread.start();
        assertTrue(entered.await(2, java.util.concurrent.TimeUnit.SECONDS));
        secondThread.start();
        release.countDown();
        firstThread.join();
        secondThread.join();

        assertEquals(List.of(extension), first.get());
        assertSame(first.get(), second.get());
        verify(context, times(1)).getBeansOfType(CrudRuntimeExtension.class);
    }

    @Test
    void transactionManagerConcurrentInitializationCoversInnerFastPath() throws Exception {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext context = mock(ApplicationContext.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        when(context.getBean(PlatformTransactionManager.class))
                .thenAnswer(
                        invocation -> {
                            entered.countDown();
                            release.await();
                            return transactionManager;
                        });

        collaborators.setApplicationContext(context);
        AtomicReference<PlatformTransactionManager> first = new AtomicReference<>();
        AtomicReference<PlatformTransactionManager> second = new AtomicReference<>();
        Thread firstThread = new Thread(() -> first.set(collaborators.transactionManager()));
        Thread secondThread = new Thread(() -> second.set(collaborators.transactionManager()));

        firstThread.start();
        assertTrue(entered.await(2, java.util.concurrent.TimeUnit.SECONDS));
        secondThread.start();
        release.countDown();
        firstThread.join();
        secondThread.join();

        assertSame(transactionManager, first.get());
        assertSame(first.get(), second.get());
        verify(context, times(1)).getBean(PlatformTransactionManager.class);
    }

    @Test
    void readDeniedAuditHooksConcurrentInitializationCoversInnerFastPath() throws Exception {
        ServiceCollaborators<Object, Object> collaborators = new ServiceCollaborators<>();
        ApplicationContext context = mock(ApplicationContext.class);
        ReadDeniedAuditHook hook = mock(ReadDeniedAuditHook.class);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);

        when(context.getBeansOfType(ReadDeniedAuditHook.class))
                .thenAnswer(
                        invocation -> {
                            entered.countDown();
                            release.await();
                            return Map.of("hook", hook);
                        });

        collaborators.setApplicationContext(context);
        AtomicReference<List<ReadDeniedAuditHook>> first = new AtomicReference<>();
        AtomicReference<List<ReadDeniedAuditHook>> second = new AtomicReference<>();
        Thread firstThread = new Thread(() -> first.set(collaborators.readDeniedAuditHooks()));
        Thread secondThread = new Thread(() -> second.set(collaborators.readDeniedAuditHooks()));

        firstThread.start();
        assertTrue(entered.await(2, java.util.concurrent.TimeUnit.SECONDS));
        secondThread.start();
        release.countDown();
        firstThread.join();
        secondThread.join();

        assertEquals(List.of(hook), first.get());
        assertSame(first.get(), second.get());
        verify(context, times(1)).getBeansOfType(ReadDeniedAuditHook.class);
    }
}
