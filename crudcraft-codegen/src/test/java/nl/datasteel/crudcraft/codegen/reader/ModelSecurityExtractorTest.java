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

package nl.datasteel.crudcraft.codegen.reader;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.security.CrudSecurityPolicy;
import nl.datasteel.crudcraft.annotations.security.RowPredicate;
import nl.datasteel.crudcraft.annotations.security.RowSecurity;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ModelSecurity;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.ScopeKind;
import nl.datasteel.crudcraft.codegen.reader.model.ModelSecurityExtractor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class ModelSecurityExtractorTest {

    static final class Holder {
        static final class NestedPolicy {
            private NestedPolicy() {}
        }
    }

    public static final class DirectPolicy implements CrudSecurityPolicy {
        @Override
        public String getSecurityExpression(CrudEndpoint endpoint) {
            return "";
        }
    }

    public static final class PrivatePolicy implements CrudSecurityPolicy {
        private PrivatePolicy() {}

        @Override
        public String getSecurityExpression(CrudEndpoint endpoint) {
            return "";
        }
    }

    public static final class HandlerA implements RowSecurityHandler<Object> {
        @Override
        public RowPredicate<Object> rowFilter() {
            return null;
        }
    }

    public static final class HandlerB implements RowSecurityHandler<Object> {
        @Override
        public RowPredicate<Object> rowFilter() {
            return null;
        }
    }

    private record TestName(String value) implements Name {
        @Override
        public boolean contentEquals(CharSequence cs) {
            return value.contentEquals(cs);
        }

        @Override
        public int length() {
            return value.length();
        }

        @Override
        public char charAt(int index) {
            return value.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return value.subSequence(start, end);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @Test
    void readsSecurityPolicyAndRowHandlers() {
        String src =
                """
                package t;
                import nl.datasteel.crudcraft.annotations.CrudEndpoint;
                import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
                import nl.datasteel.crudcraft.annotations.security.*;
                class Policy implements CrudSecurityPolicy {
                                                public String getSecurityExpression(CrudEndpoint endpoint) { return ""; }
                }
                class Handler implements RowSecurityHandler<Object> {
                                                public nl.datasteel.crudcraft.annotations.security.RowPredicate<Object> rowFilter() { return null; }
                }
                @RowSecurity(handlers=Handler.class)
                @CrudCrafted(securityPolicy=Policy.class, secure=false)
                class C {}
                """;
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        ModelSecurity sec =
                ModelSecurityExtractor.INSTANCE.extract(
                        te, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(sec.isSecure());
        assertEquals(
                CrudSecurityPolicy.class,
                sec.getSecurityPolicy());
        assertEquals(List.of("t.Handler"), sec.getRowSecurityHandlers());
    }

    @Test
    void defaultsWhenAnnotationMissing() {
        String src = "package t; class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        ModelSecurity sec =
                ModelSecurityExtractor.INSTANCE.extract(
                        te, new TestUtils.ProcessingEnvStub(elements));
        assertFalse(sec.isSecure());
        assertEquals(
                CrudSecurityPolicy.class,
                sec.getSecurityPolicy());
        assertTrue(sec.getRowSecurityHandlers().isEmpty());
        assertTrue(sec.getRowScopes().isEmpty());
        assertTrue(sec.getEndpointExpressions().isEmpty());
    }

    @Test
    void explicitUnsecuredCrudCraftedModelStaysUnsecured() {
        String src =
                """
                package t;
                import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
                @CrudCrafted(secure = false)
                class C {}
                """;
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");

        ModelSecurity sec =
                ModelSecurityExtractor.INSTANCE.extract(
                        te, new TestUtils.ProcessingEnvStub(elements));

        assertFalse(sec.isSecure());
        assertTrue(sec.getRowSecurityHandlers().isEmpty());
        assertTrue(sec.getRowScopes().isEmpty());
        assertTrue(sec.getEndpointExpressions().isEmpty());
    }

    @Test
    void readsCrudSecurityAndScopes() {
        String src =
                """
                package t;
                import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
                import nl.datasteel.crudcraft.annotations.security.*;
                @CrudCrafted
                @CrudSecurity(readRoles = {"ROLE_USER", "SUPPORT"}, writeRoles = {"EDITOR"}, deleteRoles = {"ADMIN"},
                                                endpoints = {@EndpointRbac(endpoint = nl.datasteel.crudcraft.annotations.CrudEndpoint.EXPORT, roles = {"ANALYST"})})
                @TenantScoped(field = "tenantId", claim = "tenant_id")
                @ClientScoped(field = "clientId", claim = "client_id")
                class C {}
                """;
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        ModelSecurity sec =
                ModelSecurityExtractor.INSTANCE.extract(
                        te, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(sec.hasEndpointExpressions());
        assertEquals(
                "hasAnyRole('SUPPORT', 'USER')",
                sec.getEndpointExpressions().get(CrudEndpoint.GET_ALL));
        assertEquals("hasRole('EDITOR')", sec.getEndpointExpressions().get(CrudEndpoint.POST));
        assertEquals("hasRole('ADMIN')", sec.getEndpointExpressions().get(CrudEndpoint.DELETE));
        assertEquals("hasRole('ANALYST')", sec.getEndpointExpressions().get(CrudEndpoint.EXPORT));
        assertEquals(2, sec.getRowScopes().size());
        assertEquals(ScopeKind.TENANT, sec.getRowScopes().get(0).kind());
        assertEquals(ScopeKind.CLIENT, sec.getRowScopes().get(1).kind());
    }

    @Test
    void readsOwnedByScopeAndDenyAllExpressions() {
        String src =
                """
                package t;
                import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
                import nl.datasteel.crudcraft.annotations.security.*;
                import nl.datasteel.crudcraft.annotations.CrudEndpoint;
                @CrudCrafted
                @CrudSecurity(readRoles = {}, writeRoles = {}, deleteRoles = {})
                @OwnedBy(field = "ownerId", claim = "sub")
                class C {}
                """;
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        ModelSecurity sec =
                ModelSecurityExtractor.INSTANCE.extract(
                        te, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(sec.hasEndpointExpressions());
        assertEquals("denyAll()", sec.getEndpointExpressions().get(CrudEndpoint.GET_ONE));
        assertEquals("denyAll()", sec.getEndpointExpressions().get(CrudEndpoint.POST));
        assertEquals("denyAll()", sec.getEndpointExpressions().get(CrudEndpoint.DELETE));
        assertEquals(1, sec.getRowScopes().size());
        assertEquals(ScopeKind.OWNER, sec.getRowScopes().getFirst().kind());
    }

    @Test
    void rowSecurityWithoutHandlersProducesEmptyList() {
        String src =
                """
                package t;
                import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
                import nl.datasteel.crudcraft.annotations.security.RowSecurity;
                @CrudCrafted
                @RowSecurity(handlers = {})
                class C {}
                """;
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        ModelSecurity sec =
                ModelSecurityExtractor.INSTANCE.extract(
                        te, new TestUtils.ProcessingEnvStub(elements));
        assertTrue(sec.getRowSecurityHandlers().isEmpty());
    }

    @Test
    void duplicateEndpointOverridesFailFast() {
        String src =
                """
                package t;
                import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
                import nl.datasteel.crudcraft.annotations.CrudEndpoint;
                import nl.datasteel.crudcraft.annotations.security.*;
                @CrudCrafted
                @CrudSecurity(endpoints = {
                                                @EndpointRbac(endpoint = CrudEndpoint.EXPORT, roles = {"ADMIN"}),
                                                @EndpointRbac(endpoint = CrudEndpoint.EXPORT, roles = {"USER"})
                })
                class C {}
                """;
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        assertThrows(
                IllegalStateException.class,
                () ->
                        ModelSecurityExtractor.INSTANCE.extract(
                                te, new TestUtils.ProcessingEnvStub(elements)));
    }

    @Test
    void invalidBlankRoleFailsFast() {
        String src =
                """
                package t;
                import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
                import nl.datasteel.crudcraft.annotations.security.CrudSecurity;
                @CrudCrafted
                @CrudSecurity(readRoles = {"  "})
                class C {}
                """;
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        assertThrows(
                IllegalStateException.class,
                () ->
                        ModelSecurityExtractor.INSTANCE.extract(
                                te, new TestUtils.ProcessingEnvStub(elements)));
    }

    @Test
    void rolesToExpressionNormalizesAndDeduplicates() throws Exception {
        assertEquals("denyAll()", invokeRolesToExpression(null, "readRoles"));
        assertEquals("denyAll()", invokeRolesToExpression(new String[0], "writeRoles"));
        assertEquals(
                "hasRole('USER')",
                invokeRolesToExpression(new String[] {"ROLE_USER"}, "deleteRoles"));
        assertEquals(
                "hasAnyRole('ADMIN', 'USER')",
                invokeRolesToExpression(
                        new String[] {"ROLE_USER", "ADMIN", "ROLE_USER"},
                        "endpoints[EXPORT].roles"));
        assertThrows(
                java.lang.reflect.InvocationTargetException.class,
                () -> invokeRolesToExpression(new String[] {null}, "readRoles"));
    }

    @Test
    void tryLoadPossiblyNestedResolvesCanonicalInnerClassName() throws Exception {
        Class<?> resolved =
                invokeTryLoadPossiblyNested(Holder.NestedPolicy.class.getCanonicalName());
        assertEquals(Holder.NestedPolicy.class, resolved);
    }

    @Test
    void eraseGenericsLeavesPlainTypeUntouched() throws Exception {
        Method m = ModelSecurityExtractor.class.getDeclaredMethod("eraseGenerics", String.class);
        m.setAccessible(true);
        assertEquals("", m.invoke(null, "<T>"));
        assertEquals("java.lang.String", m.invoke(null, "java.lang.String"));
        assertEquals("java.util.List", m.invoke(null, "java.util.List<java.lang.String>"));
    }

    @Test
    void rowSecurityMirrorsHandleMissingMalformedAndGenericHandlers() throws Exception {
        assertTrue(
                invokeResolveRowSecurityHandlersFromMirrors(typeWithMirrors(List.of())).isEmpty());

        AnnotationMirror otherMirror = rowSecurityMirror("other", List.of());
        assertTrue(
                invokeResolveRowSecurityHandlersFromMirrors(typeWithMirrors(List.of(otherMirror)))
                        .isEmpty());

        AnnotationMirror malformedHandlers = rowSecurityMirror("handlers", "not-a-list");
        assertTrue(
                invokeResolveRowSecurityHandlersFromMirrors(
                                typeWithMirrors(List.of(malformedHandlers)))
                        .isEmpty());

        AnnotationMirror emptyHandlers = rowSecurityMirror("handlers", List.of());
        assertTrue(
                invokeResolveRowSecurityHandlersFromMirrors(typeWithMirrors(List.of(emptyHandlers)))
                        .isEmpty());

        AnnotationValue nonTypeHandler = annotationValue("not-a-type");
        TypeMirror handlerType = mock(TypeMirror.class);
        when(handlerType.toString()).thenReturn("t.Handler<java.lang.String>");
        AnnotationValue typeHandler = annotationValue(handlerType);
        AnnotationMirror handlerMirror =
                rowSecurityMirror("handlers", List.of(nonTypeHandler, typeHandler, "raw"));

        assertEquals(
                List.of("t.Handler"),
                invokeResolveRowSecurityHandlersFromMirrors(
                        typeWithMirrors(List.of(handlerMirror))));

        TypeMirror firstType = mock(TypeMirror.class);
        when(firstType.toString()).thenReturn("t.First");
        TypeMirror secondType = mock(TypeMirror.class);
        when(secondType.toString()).thenReturn("t.Second");
        AnnotationMirror firstMirror =
                rowSecurityMirror("handlers", List.of(annotationValue(firstType)));
        AnnotationMirror secondMirror =
                rowSecurityMirror("handlers", List.of(annotationValue(secondType)));
        assertEquals(
                List.of("t.First"),
                invokeResolveRowSecurityHandlersFromMirrors(
                        typeWithMirrors(List.of(firstMirror, secondMirror))));
    }

    @Test
    void rowSecurityHandlerExtractionLogsAndReturnsEmptyOnUnexpectedFailure() throws Exception {
        RowSecurity rowSecurity =
                (RowSecurity)
                        Proxy.newProxyInstance(
                                getClass().getClassLoader(),
                                new Class<?>[] {RowSecurity.class},
                                (proxy, method, args) -> {
                                    if ("handlers".equals(method.getName())) {
                                        throw new IllegalStateException("broken");
                                    }
                                    if ("annotationType".equals(method.getName())) {
                                        return RowSecurity.class;
                                    }
                                    return method.getDefaultValue();
                                });
        TypeElement type = mock(TypeElement.class);
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(type.getAnnotation(RowSecurity.class)).thenReturn(rowSecurity);
        when(type.getAnnotationMirrors()).thenReturn(List.of());
        when(env.getMessager()).thenReturn(messager);

        assertTrue(invokeResolveRowSecurityHandlers(type, env).isEmpty());
        verify(messager)
                .printMessage(
                        eq(Diagnostic.Kind.ERROR),
                        contains("Error reading row security handler"));
    }

    @Test
    void securityPolicyResolverUsesDirectAnnotationValue() throws Exception {
        CrudCrafted craft =
                crudCraftedProxy(
                        method -> {
                            if ("securityPolicy".equals(method.getName())) {
                                return DirectPolicy.class;
                            }
                            return method.getDefaultValue();
                        });

        assertEquals(
                DirectPolicy.class,
                invokeResolveSecurityPolicy(
                        craft,
                        mock(TypeElement.class),
                        new TestUtils.ProcessingEnvStub(mock(Elements.class))));
    }

    @Test
    void securityPolicyResolverFallsBackWhenMirroredTypeCannotBeLoaded() throws Exception {
        TypeMirror mirror = mock(TypeMirror.class);
        when(mirror.toString()).thenReturn("no.such.Policy");
        CrudCrafted craft =
                crudCraftedProxy(
                        method -> {
                            if ("securityPolicy".equals(method.getName())) {
                                throw new MirroredTypeException(mirror);
                            }
                            return method.getDefaultValue();
                        });

        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);

        assertEquals(
                CrudSecurityPolicy.class,
                invokeResolveSecurityPolicy(craft, mock(TypeElement.class), env));
        verify(messager)
                .printMessage(eq(Diagnostic.Kind.NOTE), contains("Security policy class not found"));
    }

    @Test
    void securityPolicyResolverFallsBackWhenAnnotationAccessFails() throws Exception {
        CrudCrafted craft =
                crudCraftedProxy(
                        method -> {
                            if ("securityPolicy".equals(method.getName())) {
                                throw new IllegalStateException("broken");
                            }
                            return method.getDefaultValue();
                        });

        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);

        assertEquals(
                CrudSecurityPolicy.class,
                invokeResolveSecurityPolicy(craft, mock(TypeElement.class), env));
        verify(messager)
                .printMessage(eq(Diagnostic.Kind.ERROR), contains("Error reading security policy"));
    }

    @Test
    void securityPolicyResolverReadsMirroredPolicyClass() throws Exception {
        TypeMirror mirror = mock(TypeMirror.class);
        when(mirror.toString()).thenReturn(DirectPolicy.class.getCanonicalName());
        CrudCrafted craft =
                crudCraftedProxy(
                        method -> {
                            if ("securityPolicy".equals(method.getName())) {
                                throw new MirroredTypeException(mirror);
                            }
                            return method.getDefaultValue();
                        });

        assertEquals(
                DirectPolicy.class,
                invokeResolveSecurityPolicy(
                        craft,
                        mock(TypeElement.class),
                        new TestUtils.ProcessingEnvStub(mock(Elements.class))));
    }

    @Test
    void securityPolicyResolverLogsWhenMirroredPolicyCannotBeInstantiated() throws Exception {
        TypeMirror mirror = mock(TypeMirror.class);
        when(mirror.toString()).thenReturn(PrivatePolicy.class.getCanonicalName());
        CrudCrafted craft =
                crudCraftedProxy(
                        method -> {
                            if ("securityPolicy".equals(method.getName())) {
                                throw new MirroredTypeException(mirror);
                            }
                            return method.getDefaultValue();
                        });
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);

        assertEquals(
                PrivatePolicy.class,
                invokeResolveSecurityPolicy(craft, mock(TypeElement.class), env));
        verify(messager)
                .printMessage(eq(Diagnostic.Kind.ERROR), contains("Failed to instantiate class"));
    }

    @Test
    void securityPolicyResolverFallsBackWhenMirroredTypeIsNotPolicy() throws Exception {
        TypeMirror mirror = mock(TypeMirror.class);
        when(mirror.toString()).thenReturn(String.class.getCanonicalName());
        CrudCrafted craft =
                crudCraftedProxy(
                        method -> {
                            if ("securityPolicy".equals(method.getName())) {
                                throw new MirroredTypeException(mirror);
                            }
                            return method.getDefaultValue();
                        });
        ProcessingEnvironment env = mock(ProcessingEnvironment.class);
        Messager messager = mock(Messager.class);
        when(env.getMessager()).thenReturn(messager);

        assertEquals(
                CrudSecurityPolicy.class,
                invokeResolveSecurityPolicy(craft, mock(TypeElement.class), env));
        verify(messager)
                .printMessage(eq(Diagnostic.Kind.ERROR), contains("Error reading security policy"));
    }

    @Test
    void rowSecurityHandlerExtractionHandlesMirroredTypeExceptions() throws Exception {
        TypeMirror single = mock(TypeMirror.class);
        when(single.toString()).thenReturn("t.Single<java.lang.String>");
        TypeMirror first = mock(TypeMirror.class);
        when(first.toString()).thenReturn("t.First<java.lang.String>");
        TypeMirror second = mock(TypeMirror.class);
        when(second.toString()).thenReturn("t.Second");

        assertEquals(
                List.of("t.Single"),
                invokeResolveRowSecurityHandlers(
                        typeWithRowSecurityThrowing(new MirroredTypeException(single)),
                        new TestUtils.ProcessingEnvStub(mock(Elements.class))));
        assertEquals(
                List.of("t.First", "t.Second"),
                invokeResolveRowSecurityHandlers(
                        typeWithRowSecurityThrowing(
                                new MirroredTypesException(List.of(first, second))),
                        new TestUtils.ProcessingEnvStub(mock(Elements.class))));
    }

    @Test
    void rowSecurityHandlerExtractionReadsRuntimeHandlerClasses() throws Exception {
        RowSecurity rowSecurity =
                rowSecurityProxy(
                        method -> {
                            if ("handlers".equals(method.getName())) {
                                return new Class<?>[] {HandlerA.class, HandlerB.class};
                            }
                            return method.getDefaultValue();
                        });
        TypeElement type = mock(TypeElement.class);
        when(type.getAnnotation(RowSecurity.class)).thenReturn(rowSecurity);
        when(type.getAnnotationMirrors()).thenReturn(List.of());

        assertEquals(
                List.of(HandlerA.class.getCanonicalName(), HandlerB.class.getCanonicalName()),
                invokeResolveRowSecurityHandlers(
                        type, new TestUtils.ProcessingEnvStub(mock(Elements.class))));
    }

    @Test
    void tryLoadPossiblyNestedResolvesDirectClassName() throws Exception {
        assertEquals(
                DirectPolicy.class,
                invokeTryLoadPossiblyNested(DirectPolicy.class.getCanonicalName()));
    }

    private static String invokeRolesToExpression(String[] roles, String source) throws Exception {
        Method m =
                ModelSecurityExtractor.class.getDeclaredMethod(
                        "rolesToExpression", String[].class, String.class);
        m.setAccessible(true);
        return (String) m.invoke(null, roles, source);
    }

    private static Class<?> invokeTryLoadPossiblyNested(String canonicalName) throws Exception {
        Method m =
                ModelSecurityExtractor.class.getDeclaredMethod(
                        "tryLoadPossiblyNested", String.class);
        m.setAccessible(true);
        return (Class<?>) m.invoke(null, canonicalName);
    }

    private static Class<? extends CrudSecurityPolicy> invokeResolveSecurityPolicy(
            CrudCrafted craft,
            TypeElement type,
            javax.annotation.processing.ProcessingEnvironment env)
            throws Exception {
        Method m =
                ModelSecurityExtractor.class.getDeclaredMethod(
                        "resolveSecurityPolicy",
                        CrudCrafted.class,
                        TypeElement.class,
                        javax.annotation.processing.ProcessingEnvironment.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Class<? extends CrudSecurityPolicy> result =
                (Class<? extends CrudSecurityPolicy>) m.invoke(null, craft, type, env);
        return result;
    }

    private static List<String> invokeResolveRowSecurityHandlersFromMirrors(TypeElement type)
            throws Exception {
        Method m =
                ModelSecurityExtractor.class.getDeclaredMethod(
                        "resolveRowSecurityHandlersFromMirrors", TypeElement.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) m.invoke(null, type);
        return result;
    }

    private static List<String> invokeResolveRowSecurityHandlers(
            TypeElement type, javax.annotation.processing.ProcessingEnvironment env)
            throws Exception {
        Method m =
                ModelSecurityExtractor.class.getDeclaredMethod(
                        "resolveRowSecurityHandlers",
                        TypeElement.class,
                        javax.annotation.processing.ProcessingEnvironment.class);
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<String> result = (List<String>) m.invoke(null, type, env);
        return result;
    }

    private static TypeElement typeWithMirrors(List<AnnotationMirror> mirrors) {
        TypeElement type = mock(TypeElement.class);
        doReturn(mirrors).when(type).getAnnotationMirrors();
        return type;
    }

    private static TypeElement typeWithRowSecurityThrowing(RuntimeException failure) {
        TypeElement type = mock(TypeElement.class);
        when(type.getAnnotation(RowSecurity.class))
                .thenReturn(
                        rowSecurityProxy(
                                method -> {
                                    if ("handlers".equals(method.getName())) {
                                        throw failure;
                                    }
                                    return method.getDefaultValue();
                                }));
        when(type.getAnnotationMirrors()).thenReturn(List.of());
        return type;
    }

    private static CrudCrafted crudCraftedProxy(AnnotationAnswer answer) {
        return (CrudCrafted)
                Proxy.newProxyInstance(
                        ModelSecurityExtractorTest.class.getClassLoader(),
                        new Class<?>[] {CrudCrafted.class},
                        (proxy, method, args) -> {
                            if ("annotationType".equals(method.getName())) {
                                return CrudCrafted.class;
                            }
                            return answer.answer(method);
                        });
    }

    private static RowSecurity rowSecurityProxy(AnnotationAnswer answer) {
        return (RowSecurity)
                Proxy.newProxyInstance(
                        ModelSecurityExtractorTest.class.getClassLoader(),
                        new Class<?>[] {RowSecurity.class},
                        (proxy, method, args) -> {
                            if ("annotationType".equals(method.getName())) {
                                return RowSecurity.class;
                            }
                            return answer.answer(method);
                        });
    }

    private interface AnnotationAnswer {
        Object answer(Method method);
    }

    private static AnnotationMirror rowSecurityMirror(String elementName, Object value) {
        AnnotationMirror mirror = mock(AnnotationMirror.class);
        ExecutableElement key = mock(ExecutableElement.class);
        AnnotationValue annotationValue = annotationValue(value);
        javax.lang.model.type.DeclaredType annotationType =
                annotationType(RowSecurity.class.getCanonicalName());
        when(mirror.getAnnotationType()).thenReturn(annotationType);
        when(key.getSimpleName()).thenReturn(new TestName(elementName));
        doReturn(Map.of(key, annotationValue)).when(mirror).getElementValues();
        return mirror;
    }

    private static javax.lang.model.type.DeclaredType annotationType(String name) {
        javax.lang.model.type.DeclaredType type = mock(javax.lang.model.type.DeclaredType.class);
        when(type.toString()).thenReturn(name);
        return type;
    }

    private static AnnotationValue annotationValue(Object value) {
        AnnotationValue annotationValue = mock(AnnotationValue.class);
        when(annotationValue.getValue()).thenReturn(value);
        return annotationValue;
    }
}
