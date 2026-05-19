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

import com.google.testing.compile.JavaFileObjects;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.CrudEndpointPolicy;
import nl.datasteel.crudcraft.annotations.CrudTemplate;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.codegen.CompilationTestUtils;
import nl.datasteel.crudcraft.codegen.descriptor.model.part.EndpointOptions;
import nl.datasteel.crudcraft.codegen.reader.model.EndpointOptionsExtractor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class EndpointOptionsExtractorTest {
    public static final class EmptyEndpointPolicy implements CrudEndpointPolicy {
        @Override
        public Set<CrudEndpoint> resolveEndpoints() {
            return Set.of();
        }

        @Override
        public String name() {
            return "empty-endpoint-policy";
        }
    }

    public static final class UninstantiableEndpointPolicy implements CrudEndpointPolicy {
        private UninstantiableEndpointPolicy() {}

        @Override
        public Set<CrudEndpoint> resolveEndpoints() {
            return Set.of(CrudEndpoint.GET_ONE);
        }

        @Override
        public String name() {
            return "uninstantiable-endpoint-policy";
        }
    }

    private static final class RecordingMessager implements Messager {
        private final List<String> warnings = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
            record(kind, msg);
        }

        @Override
        public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
            record(kind, msg);
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a) {
            record(kind, msg);
        }

        @Override
        public void printMessage(
                Diagnostic.Kind kind,
                CharSequence msg,
                Element e,
                javax.lang.model.element.AnnotationMirror a,
                javax.lang.model.element.AnnotationValue v) {
            record(kind, msg);
        }

        private void record(Diagnostic.Kind kind, CharSequence msg) {
            if (kind == Diagnostic.Kind.WARNING) {
                warnings.add(msg.toString());
            } else if (kind == Diagnostic.Kind.ERROR) {
                errors.add(msg.toString());
            }
        }
    }

    private static final class RecordingEnv implements ProcessingEnvironment {
        private final Elements elements;
        private final RecordingMessager messager;

        private RecordingEnv(Elements elements, RecordingMessager messager) {
            this.elements = elements;
            this.messager = messager;
        }

        @Override
        public Map<String, String> getOptions() {
            return Map.of();
        }

        @Override
        public Messager getMessager() {
            return messager;
        }

        @Override
        public Filer getFiler() {
            return null;
        }

        @Override
        public Elements getElementUtils() {
            return elements;
        }

        @Override
        public Types getTypeUtils() {
            return null;
        }

        @Override
        public SourceVersion getSourceVersion() {
            return SourceVersion.latest();
        }

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }
    }

    @Test
    void extractsTemplateOmitIncludeAndPolicy() {
        String src =
                "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;import"
                    + " nl.datasteel.crudcraft.annotations.*;@CrudCrafted(template=CrudTemplate.FULL,"
                    + " omitEndpoints=CrudEndpoint.DELETE, includeEndpoints=CrudEndpoint.GET_ONE)"
                    + " class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        EndpointOptions opts =
                EndpointOptionsExtractor.INSTANCE.extract(
                        te, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(CrudTemplate.FULL, opts.getTemplate());
        assertArrayEquals(new CrudEndpoint[] {CrudEndpoint.DELETE}, opts.getOmitEndpoints());
        assertArrayEquals(new CrudEndpoint[] {CrudEndpoint.GET_ONE}, opts.getIncludeEndpoints());
        assertEquals(CrudTemplate.class, opts.getEndpointPolicy());
    }

    @Test
    void failsWhenPolicyCannotInstantiate() {
        String src =
                "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;import"
                    + " nl.datasteel.crudcraft.annotations.*;import java.util.Set;class Bad"
                    + " implements CrudEndpointPolicy { private Bad(){} public Set<CrudEndpoint>"
                    + " resolveEndpoints(){return Set.of();} public String name(){return"
                    + " \"Bad\";}}@CrudCrafted(endpointPolicy=Bad.class) class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        assertThrows(
                IllegalStateException.class,
                () ->
                        EndpointOptionsExtractor.INSTANCE.extract(
                                te, new TestUtils.ProcessingEnvStub(elements)));
    }

    @Test
    void defaultsWhenAnnotationMissing() {
        String src = "package t; class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");
        EndpointOptions opts =
                EndpointOptionsExtractor.INSTANCE.extract(
                        te, new TestUtils.ProcessingEnvStub(elements));
        assertEquals(CrudTemplate.FULL, opts.getTemplate());
        assertEquals(0, opts.getOmitEndpoints().length);
        assertEquals(0, opts.getIncludeEndpoints().length);
        assertEquals(CrudTemplate.class, opts.getEndpointPolicy());
    }

    @Test
    void logsWarningsForEmptyAndRedundantEndpointResolution() {
        String emptySrc =
                "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;import"
                        + " nl.datasteel.crudcraft.annotations.*;"
                        + "@CrudCrafted(template=CrudTemplate.VALIDATION_ONLY,"
                        + " omitEndpoints={CrudEndpoint.VALIDATE, CrudEndpoint.DELETE}) class C {}";
        String redundantIncludeSrc =
                "package t; import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;import"
                        + " nl.datasteel.crudcraft.annotations.*;"
                        + "@CrudCrafted(template=CrudTemplate.VALIDATION_ONLY,"
                        + " includeEndpoints={CrudEndpoint.VALIDATE}) class D {}";
        Elements elements =
                CompilationTestUtils.elements(
                        JavaFileObjects.forSourceString("t.C", emptySrc),
                        JavaFileObjects.forSourceString("t.D", redundantIncludeSrc));
        RecordingMessager messager = new RecordingMessager();
        EndpointOptionsExtractor.INSTANCE.extract(
                elements.getTypeElement("t.C"), new RecordingEnv(elements, messager));
        EndpointOptionsExtractor.INSTANCE.extract(
                elements.getTypeElement("t.D"), new RecordingEnv(elements, messager));

        assertTrue(
                messager.warnings.stream().anyMatch(msg -> msg.contains("No endpoints resolved")),
                "Expected warning when effective endpoint set is empty before include pass");
        assertTrue(
                messager.warnings.stream()
                        .anyMatch(
                                msg ->
                                        msg.contains(
                                                "Omitted endpoint DELETE is not part of the base"
                                                        + " template")),
                "Expected warning for omitting non-template endpoint");
        assertTrue(
                messager.warnings.stream()
                        .anyMatch(
                                msg ->
                                        msg.contains(
                                                "Included endpoint VALIDATE is already part of the"
                                                        + " template")),
                "Expected warning for redundant include");
    }

    @Test
    void supportsCustomEndpointPolicyClass() {
        String src =
                "package t; import java.util.Set; import nl.datasteel.crudcraft.annotations.*;"
                    + " import"
                    + " nl.datasteel.crudcraft.annotations.classes.CrudCrafted;@CrudCrafted(endpointPolicy=nl.datasteel.crudcraft.codegen.reader.TestEndpointPolicy.class)"
                    + " class C {}";
        Elements elements = CompilationTestUtils.elements("t.C", src);
        TypeElement te = elements.getTypeElement("t.C");

        EndpointOptions options =
                EndpointOptionsExtractor.INSTANCE.extract(
                        te, new TestUtils.ProcessingEnvStub(elements));

        assertEquals(TestEndpointPolicy.class.getName(), options.getEndpointPolicy().getName());
    }

    @Test
    void directPolicyClassReturnIsAcceptedOutsideAnnotationProcessing() throws Exception {
        Elements elements = CompilationTestUtils.elements("t.C", "package t; class C {}");
        Class<? extends CrudEndpointPolicy> policy =
                invokeExtractPolicyClass(
                        crudCraftedReturning(TestEndpointPolicy.class),
                        new TestUtils.ProcessingEnvStub(elements),
                        elements.getTypeElement("t.C"));

        assertEquals(TestEndpointPolicy.class, policy);
    }

    @Test
    void policyClassThatDoesNotImplementInterfaceLogsError() throws Exception {
        Elements elements = CompilationTestUtils.elements("t.C", "package t; class C {}");
        RecordingMessager messager = new RecordingMessager();
        RecordingEnv env = new RecordingEnv(elements, messager);

        InvocationTargetException thrown =
                assertThrows(
                        InvocationTargetException.class,
                        () ->
                                invokeExtractPolicyClass(
                                        crudCraftedMirroring("java.lang.String"),
                                        env,
                                        elements.getTypeElement("t.C")));

        assertTrue(thrown.getCause() instanceof IllegalStateException);
        assertTrue(
                messager.errors.stream()
                        .anyMatch(
                                msg ->
                                        msg.contains(
                                                "Policy class does not implement"
                                                        + " CrudEndpointPolicy")));
    }

    @Test
    void missingPolicyClassLogsError() throws Exception {
        Elements elements = CompilationTestUtils.elements("t.C", "package t; class C {}");
        RecordingMessager messager = new RecordingMessager();
        RecordingEnv env = new RecordingEnv(elements, messager);

        InvocationTargetException thrown =
                assertThrows(
                        InvocationTargetException.class,
                        () ->
                                invokeExtractPolicyClass(
                                        crudCraftedMirroring("missing.Policy"),
                                        env,
                                        elements.getTypeElement("t.C")));

        assertTrue(thrown.getCause() instanceof IllegalStateException);
        assertTrue(
                messager.errors.stream()
                        .anyMatch(msg -> msg.contains("Policy class not found: missing.Policy")));
    }

    @Test
    void endpointPolicyResolutionFailureLogsError() throws Exception {
        Elements elements = CompilationTestUtils.elements("t.C", "package t; class C {}");
        RecordingMessager messager = new RecordingMessager();
        RecordingEnv env = new RecordingEnv(elements, messager);

        InvocationTargetException thrown =
                assertThrows(
                        InvocationTargetException.class,
                        () ->
                                invokeExtractPolicyClass(
                                        crudCraftedThrowing(new IllegalArgumentException("boom")),
                                        env,
                                        elements.getTypeElement("t.C")));

        assertTrue(thrown.getCause() instanceof IllegalStateException);
        assertTrue(
                messager.errors.stream()
                        .anyMatch(msg -> msg.contains("Failed to resolve endpoint policy class")));
    }

    @Test
    void endpointPolicyInstantiationFailureLogsError() throws Exception {
        Elements elements = CompilationTestUtils.elements("t.C", "package t; class C {}");
        RecordingMessager messager = new RecordingMessager();
        RecordingEnv env = new RecordingEnv(elements, messager);

        InvocationTargetException thrown =
                assertThrows(
                        InvocationTargetException.class,
                        () ->
                                invokeValidatePolicyApplication(
                                        UninstantiableEndpointPolicy.class,
                                        CrudTemplate.FULL,
                                        new CrudEndpoint[0],
                                        new CrudEndpoint[0],
                                        env,
                                        elements.getTypeElement("t.C")));

        assertTrue(thrown.getCause() instanceof IllegalStateException);
        assertTrue(
                messager.errors.stream()
                        .anyMatch(msg -> msg.contains("Could not instantiate endpoint policy")));
    }

    @Test
    void emptyEndpointPolicyLogsWarning() throws Exception {
        Elements elements = CompilationTestUtils.elements("t.C", "package t; class C {}");
        RecordingMessager messager = new RecordingMessager();
        RecordingEnv env = new RecordingEnv(elements, messager);

        invokeValidatePolicyApplication(
                EmptyEndpointPolicy.class,
                CrudTemplate.FULL,
                new CrudEndpoint[0],
                new CrudEndpoint[0],
                env,
                elements.getTypeElement("t.C"));

        assertTrue(
                messager.warnings.stream().anyMatch(msg -> msg.contains("No endpoints resolved")));
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends CrudEndpointPolicy> invokeExtractPolicyClass(
            CrudCrafted annotation, ProcessingEnvironment env, TypeElement cls) throws Exception {
        Method method =
                EndpointOptionsExtractor.class.getDeclaredMethod(
                        "extractPolicyClass",
                        CrudCrafted.class,
                        ProcessingEnvironment.class,
                        TypeElement.class);
        method.setAccessible(true);
        return (Class<? extends CrudEndpointPolicy>)
                method.invoke(EndpointOptionsExtractor.INSTANCE, annotation, env, cls);
    }

    private static void invokeValidatePolicyApplication(
            Class<? extends CrudEndpointPolicy> policyClass,
            CrudTemplate template,
            CrudEndpoint[] omit,
            CrudEndpoint[] include,
            ProcessingEnvironment env,
            TypeElement cls)
            throws Exception {
        Method method =
                EndpointOptionsExtractor.class.getDeclaredMethod(
                        "validatePolicyApplication",
                        Class.class,
                        CrudTemplate.class,
                        CrudEndpoint[].class,
                        CrudEndpoint[].class,
                        ProcessingEnvironment.class,
                        TypeElement.class);
        method.setAccessible(true);
        method.invoke(EndpointOptionsExtractor.INSTANCE, policyClass, template, omit, include, env, cls);
    }

    private static CrudCrafted crudCraftedReturning(
            Class<? extends CrudEndpointPolicy> policyClass) {
        return crudCraftedProxy(policyClass, null, null);
    }

    private static CrudCrafted crudCraftedMirroring(String fqn) {
        TypeMirror mirror =
                (TypeMirror)
                        Proxy.newProxyInstance(
                                EndpointOptionsExtractorTest.class.getClassLoader(),
                                new Class[] {TypeMirror.class},
                                (p, m, a) ->
                                        switch (m.getName()) {
                                            case "toString" -> fqn;
                                            default -> null;
                                        });
        return crudCraftedProxy(null, new MirroredTypeException(mirror), null);
    }

    private static CrudCrafted crudCraftedThrowing(RuntimeException exception) {
        return crudCraftedProxy(null, null, exception);
    }

    private static CrudCrafted crudCraftedProxy(
            Class<? extends CrudEndpointPolicy> policyClass,
            MirroredTypeException mirrored,
            RuntimeException exception) {
        return (CrudCrafted)
                Proxy.newProxyInstance(
                        EndpointOptionsExtractorTest.class.getClassLoader(),
                        new Class[] {CrudCrafted.class},
                        (p, m, a) ->
                                switch (m.getName()) {
                                    case "endpointPolicy" -> {
                                        if (exception != null) {
                                            throw exception;
                                        }
                                        if (mirrored != null) {
                                            throw mirrored;
                                        }
                                        yield policyClass;
                                    }
                                    case "annotationType" -> CrudCrafted.class;
                                    default -> m.getDefaultValue();
                                });
    }
}
