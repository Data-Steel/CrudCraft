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

package nl.datasteel.crudcraft.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.export.ExportExclude;
import nl.datasteel.crudcraft.annotations.fields.BatchFetched;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.annotations.fields.EnumString;
import nl.datasteel.crudcraft.annotations.fields.ProjectionField;
import nl.datasteel.crudcraft.annotations.fields.Request;
import nl.datasteel.crudcraft.annotations.fields.Searchable;
import nl.datasteel.crudcraft.annotations.security.ClientScoped;
import nl.datasteel.crudcraft.annotations.security.CrudSecurity;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.annotations.security.EndpointRbac;
import nl.datasteel.crudcraft.annotations.security.FieldSecurity;
import nl.datasteel.crudcraft.annotations.security.OwnedBy;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.annotations.security.TenantScoped;
import nl.datasteel.crudcraft.annotations.security.WritePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


class AnnotationContractsTest {

    @Test
    void crudCraftedContractIsStable() throws NoSuchMethodException {
        assertArrayEquals(new ElementType[] {ElementType.TYPE}, target(CrudCrafted.class));
        assertEquals(RetentionPolicy.SOURCE, retention(CrudCrafted.class));
        assertEquals(false, CrudCrafted.class.getMethod("editable").getDefaultValue());
        assertEquals("", CrudCrafted.class.getMethod("basePackage").getDefaultValue());
        assertEquals(CrudTemplate.FULL, CrudCrafted.class.getMethod("template").getDefaultValue());
        assertArrayEquals(
                new CrudEndpoint[] {},
                (CrudEndpoint[]) CrudCrafted.class.getMethod("omitEndpoints").getDefaultValue());
        assertArrayEquals(
                new CrudEndpoint[] {},
                (CrudEndpoint[]) CrudCrafted.class.getMethod("includeEndpoints").getDefaultValue());
        assertEquals(
                CrudTemplate.class,
                CrudCrafted.class.getMethod("endpointPolicy").getDefaultValue());
        assertEquals(false, CrudCrafted.class.getMethod("secure").getDefaultValue());
        assertEquals(
                CrudSecurityPolicy.class,
                CrudCrafted.class.getMethod("securityPolicy").getDefaultValue());
    }

    @Test
    void fieldAnnotationContractsAreStable() throws NoSuchMethodException {
        assertArrayEquals(new ElementType[] {ElementType.FIELD}, target(Dto.class));
        assertEquals(RetentionPolicy.CLASS, retention(Dto.class));
        assertEquals(false, Dto.class.getMethod("ref").getDefaultValue());
        assertArrayEquals(
                new String[] {}, (String[]) Dto.class.getMethod("value").getDefaultValue());

        assertArrayEquals(new ElementType[] {ElementType.FIELD}, target(EnumString.class));
        assertEquals(RetentionPolicy.CLASS, retention(EnumString.class));
        assertArrayEquals(
                new String[] {}, (String[]) EnumString.class.getMethod("values").getDefaultValue());

        assertArrayEquals(new ElementType[] {ElementType.FIELD}, target(Request.class));
        assertEquals(RetentionPolicy.CLASS, retention(Request.class));

        assertArrayEquals(new ElementType[] {ElementType.FIELD}, target(BatchFetched.class));
        assertEquals(RetentionPolicy.CLASS, retention(BatchFetched.class));

        assertArrayEquals(
                new ElementType[] {ElementType.FIELD, ElementType.RECORD_COMPONENT},
                target(ProjectionField.class));
        assertEquals(RetentionPolicy.RUNTIME, retention(ProjectionField.class));
        assertEquals(null, ProjectionField.class.getMethod("value").getDefaultValue());

        assertArrayEquals(
                new ElementType[] {ElementType.FIELD, ElementType.TYPE},
                target(Searchable.class));
        assertEquals(RetentionPolicy.CLASS, retention(Searchable.class));
        assertArrayEquals(
                new SearchOperator[] {},
                (SearchOperator[]) Searchable.class.getMethod("operators").getDefaultValue());
        assertEquals(1, Searchable.class.getMethod("depth").getDefaultValue());
    }

    @Test
    void securityAndExportAnnotationContractsAreStable() throws NoSuchMethodException {
        assertArrayEquals(new ElementType[] {ElementType.TYPE}, target(ClientScoped.class));
        assertEquals(RetentionPolicy.SOURCE, retention(ClientScoped.class));
        assertEquals("clientId", ClientScoped.class.getMethod("field").getDefaultValue());
        assertEquals("client_id", ClientScoped.class.getMethod("claim").getDefaultValue());

        assertArrayEquals(new ElementType[] {ElementType.TYPE}, target(OwnedBy.class));
        assertEquals(RetentionPolicy.SOURCE, retention(OwnedBy.class));
        assertEquals("ownerId", OwnedBy.class.getMethod("field").getDefaultValue());
        assertEquals("sub", OwnedBy.class.getMethod("claim").getDefaultValue());

        assertArrayEquals(new ElementType[] {ElementType.TYPE}, target(TenantScoped.class));
        assertEquals(RetentionPolicy.SOURCE, retention(TenantScoped.class));
        assertEquals("tenantId", TenantScoped.class.getMethod("field").getDefaultValue());
        assertEquals("tenant_id", TenantScoped.class.getMethod("claim").getDefaultValue());

        assertArrayEquals(new ElementType[] {ElementType.TYPE}, target(CrudSecurity.class));
        assertEquals(RetentionPolicy.SOURCE, retention(CrudSecurity.class));
        assertArrayEquals(
                new String[] {},
                (String[]) CrudSecurity.class.getMethod("readRoles").getDefaultValue());
        assertArrayEquals(
                new String[] {},
                (String[]) CrudSecurity.class.getMethod("writeRoles").getDefaultValue());
        assertArrayEquals(
                new String[] {},
                (String[]) CrudSecurity.class.getMethod("deleteRoles").getDefaultValue());
        assertArrayEquals(
                new EndpointRbac[] {},
                (EndpointRbac[]) CrudSecurity.class.getMethod("endpoints").getDefaultValue());

        assertArrayEquals(
                new ElementType[] {
                    ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT
                },
                target(FieldSecurity.class));
        assertEquals(RetentionPolicy.RUNTIME, retention(FieldSecurity.class));
        assertArrayEquals(
                new String[] {"ALL"},
                (String[]) FieldSecurity.class.getMethod("readRoles").getDefaultValue());
        assertArrayEquals(
                new String[] {"ALL"},
                (String[]) FieldSecurity.class.getMethod("writeRoles").getDefaultValue());
        assertEquals(
                WritePolicy.SKIP_ON_DENIED,
                FieldSecurity.class.getMethod("writePolicy").getDefaultValue());

        assertArrayEquals(new ElementType[] {}, target(EndpointRbac.class));
        assertEquals(RetentionPolicy.SOURCE, retention(EndpointRbac.class));

        assertArrayEquals(new ElementType[] {ElementType.TYPE}, target(RowSecurity.class));
        assertEquals(RetentionPolicy.RUNTIME, retention(RowSecurity.class));

        assertArrayEquals(new ElementType[] {ElementType.FIELD}, target(ExportExclude.class));
        assertEquals(RetentionPolicy.RUNTIME, retention(ExportExclude.class));
    }

    @Test
    void internalOnlyMarksNonSpiElementsForRuntimeAndTooling() throws NoSuchMethodException {
        assertArrayEquals(
                new ElementType[] {
                    ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR,
                    ElementType.FIELD
                },
                target(InternalOnly.class));
        assertEquals(RetentionPolicy.RUNTIME, retention(InternalOnly.class));
        assertEquals(
                "Internal CrudCraft implementation detail.",
                InternalOnly.class.getMethod("value").getDefaultValue());
    }

    private static ElementType[] target(Class<?> annotationType) {
        return annotationType.getAnnotation(Target.class).value();
    }

    private static RetentionPolicy retention(Class<?> annotationType) {
        return annotationType.getAnnotation(Retention.class).value();
    }
}
