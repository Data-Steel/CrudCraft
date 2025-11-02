/*
 * /*
 *  * Copyright (c) 2025 CrudCraft contributors
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  */
 */
package nl.datasteel.crudcraft.codegen;

import com.google.auto.service.AutoService;
import jakarta.persistence.Embeddable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.reader.AnnotationModelReader;
import nl.datasteel.crudcraft.codegen.writer.WriteContext;
import nl.datasteel.crudcraft.codegen.writer.WriterRegistry;

/**
 * Annotation processor for CrudCraft.
 * This processor scans for classes annotated with CrudCrafted and Embeddable,
 * and generates the necessary code based on the annotations present.
 * It supports various annotations related to CRUD operations,
 * including Dto, Request, Response, Searchable, and others.
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
    "nl.datasteel.crudcraft.annotations.classes.CrudCrafted",
    "nl.datasteel.crudcraft.annotations.classes.CrudCraftExtension",
    "nl.datasteel.crudcraft.annotations.fields.Dto",
    "nl.datasteel.crudcraft.annotations.fields.Request",
    "nl.datasteel.crudcraft.annotations.fields.Response",
    "nl.datasteel.crudcraft.annotations.fields.Searchable",
    "jakarta.persistence.Embeddable",
})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class CrudCraftProcessor extends AbstractProcessor {

    /**
     * Processing environment provided by the annotation processing framework.
     * It contains information about the current processing round and allows
     * access to the file system, messager, and other utilities.
     */
    private ProcessingEnvironment env;

    /**
     * Messager for logging messages during annotation processing.
     * It is used to report errors, warnings, and informational messages.
     */
    private Messager messager;

    /**
     * Initializes the processor with the processing environment.
     * This method is called by the annotation processing framework
     * before any processing occurs.
     *
     * @param env The processing environment provided by the framework.
     */
    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        this.messager = env.getMessager();
        this.env = env;
    }

    /**
     * Processes the annotations found in the current round.
     * It collects all model descriptors from classes annotated with CrudCrafted and Embeddable,
     * and writes the generated code using the WriterRegistry.
     *
     * @param annotations The set of annotations to process.
     * @param roundEnv The current round environment.
     * @return true if the annotations were processed successfully, false otherwise.
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        WriteContext ctx = new WriteContext(env);

        // Find every class annotated with @CrudCrafted
        List<ModelDescriptor> modelDescriptors = getModelDescriptors(roundEnv);

        // Loop through all model descriptors and write the code they require
        for (ModelDescriptor modelDescriptor : modelDescriptors) {
            try {
                WriterRegistry.writeAll(modelDescriptor, ctx);
            } catch (Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        "CrudCraftProcessor failed for " + modelDescriptor.getName()
                                + ": " + e.getMessage());
            }
        }
        return true;
    }

    /**
     * Collects all model descriptors from the round environment.
     * This includes both CrudCrafted and Embeddable classes.
     *
     * @param roundEnv The current round environment.
     * @return A list of model descriptors.
     */
    private List<ModelDescriptor> getModelDescriptors(RoundEnvironment roundEnv) {
        // Initialize a list to hold the model descriptors
        List<ModelDescriptor> descriptors = new ArrayList<>();

        // Read CrudCrafted classes
        for (Element e : roundEnv.getElementsAnnotatedWith(CrudCrafted.class)) {
            if (e.getKind() != ElementKind.CLASS) {
                continue;
            }
            descriptors.add(AnnotationModelReader.parse(e, env));
        }

        // Read Embeddable classes
        for (Element e : roundEnv.getElementsAnnotatedWith(Embeddable.class)) {
            if (e.getKind() != ElementKind.CLASS) {
                continue;
            }
            descriptors.add(AnnotationModelReader.parse(e, env));
        }

        return descriptors;
    }
}
