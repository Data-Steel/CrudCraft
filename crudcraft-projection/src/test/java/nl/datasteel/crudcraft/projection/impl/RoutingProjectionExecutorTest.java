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

package nl.datasteel.crudcraft.projection.impl;

import nl.datasteel.crudcraft.projection.api.ProjectionExecutor;
import nl.datasteel.crudcraft.projection.api.ProjectionExecutorProvider;
import nl.datasteel.crudcraft.projection.api.ProjectionQuery;
import nl.datasteel.crudcraft.projection.api.ProjectionResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoutingProjectionExecutorTest {

    @Test
    void delegatesToFirstSupportingProvider() {
        ProjectionExecutorProvider p1 = mock(ProjectionExecutorProvider.class);
        ProjectionExecutorProvider p2 = mock(ProjectionExecutorProvider.class);
        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        ProjectionExecutor executor = mock(ProjectionExecutor.class);
        ProjectionResult<String> result = new ProjectionResult<>(List.of("x"),1);

        when(p1.supports(query)).thenReturn(false);
        when(p2.supports(query)).thenReturn(true);
        when(p2.getExecutor()).thenReturn(executor);
        when(executor.project(Object.class, String.class, query)).thenReturn(result);

        RoutingProjectionExecutor routing = new RoutingProjectionExecutor(List.of(p1,p2));
        ProjectionResult<String> actual = routing.project(Object.class, String.class, query);

        assertSame(result, actual);
        verify(p1).supports(query);
        verify(p2).supports(query);
        verify(p2).getExecutor();
        verify(executor).project(Object.class, String.class, query);
        verifyNoMoreInteractions(p1,p2,executor);
    }

    @Test
    void throwsWhenNoProviderSupports() {
        ProjectionExecutorProvider provider = mock(ProjectionExecutorProvider.class);
        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        when(provider.supports(query)).thenReturn(false);

        RoutingProjectionExecutor routing = new RoutingProjectionExecutor(List.of(provider));
        assertThrows(IllegalStateException.class, () -> routing.project(Object.class, Object.class, query));
    }

    @Test
    void throwsWhenProviderListEmpty() {
        RoutingProjectionExecutor routing = new RoutingProjectionExecutor(List.of());
        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        assertThrows(IllegalStateException.class, () -> routing.project(Object.class, Object.class, query));
    }

    @Test
    void usesFirstProviderWhenMultipleSupport() {
        ProjectionExecutorProvider p1 = mock(ProjectionExecutorProvider.class);
        ProjectionExecutorProvider p2 = mock(ProjectionExecutorProvider.class);
        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        ProjectionExecutor exec1 = mock(ProjectionExecutor.class);
        ProjectionExecutor exec2 = mock(ProjectionExecutor.class);
        ProjectionResult<String> result = new ProjectionResult<>(List.of("y"),1);
        when(p1.supports(query)).thenReturn(true);
        when(p2.supports(query)).thenReturn(true);
        when(p1.getExecutor()).thenReturn(exec1);
        when(exec1.project(Object.class, String.class, query)).thenReturn(result);

        RoutingProjectionExecutor routing = new RoutingProjectionExecutor(List.of(p1,p2));
        ProjectionResult<String> actual = routing.project(Object.class, String.class, query);

        assertSame(result, actual);
        verify(p1).supports(query);
        verify(p1).getExecutor();
        verify(exec1).project(Object.class, String.class, query);
        verify(p2, never()).getExecutor();
    }

    @Test
    void stopsEvaluatingProvidersAfterMatch() {
        ProjectionExecutorProvider p1 = mock(ProjectionExecutorProvider.class);
        ProjectionExecutorProvider p2 = mock(ProjectionExecutorProvider.class);
        ProjectionQuery<Object> query = mock(ProjectionQuery.class);
        ProjectionExecutor exec1 = mock(ProjectionExecutor.class);
        ProjectionResult<String> result = new ProjectionResult<>(List.of("z"),1);

        when(p1.supports(query)).thenReturn(true);
        when(p1.getExecutor()).thenReturn(exec1);
        when(exec1.project(Object.class, String.class, query)).thenReturn(result);

        RoutingProjectionExecutor routing = new RoutingProjectionExecutor(List.of(p1,p2));
        routing.project(Object.class, String.class, query);

        verify(p2, never()).supports(query);
    }
}
