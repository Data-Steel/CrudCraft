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

package nl.datasteel.crudcraft.codegen.writer;

import com.palantir.javapoet.JavaFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.codegen.descriptor.field.FieldDescriptor;
import nl.datasteel.crudcraft.codegen.descriptor.model.ModelDescriptor;
import nl.datasteel.crudcraft.codegen.util.StringCase;


/**
 * Generates an insomnia.json export file containing CRUD endpoints for CrudCraft entities.
 */
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class InsomniaGenerator implements Generator {

    private static final String INSOMNIA_FILE = "insomnia.json";
    private static final String OUTPUT_DIR_OPTION = "crudcraft.insomnia.outputDir";
    private static final String REPRODUCIBLE_EXPORT_DATE = "2026-01-01T00:00:00Z";
    private final Path outputDirectory;
    private final Predicate<Path> writablePath;

    /** Creates the Insomnia collection generator. */
    public InsomniaGenerator() {
        this(null);
    }

    InsomniaGenerator(Path outputDirectory) {
        this(outputDirectory, Files::isWritable);
    }

    InsomniaGenerator(Path outputDirectory, Predicate<Path> writablePath) {
        this.outputDirectory = outputDirectory;
        this.writablePath = writablePath;
    }

    @Override
    public List<JavaFile> generate(ModelDescriptor model, WriteContext ctx) {
        ctx.registerInsomniaModel(model);
        return List.of();
    }

    @Override
    public boolean requiresCrudEntity() {
        return true;
    }

    @Override
    public int order() {
        return 1000;
    }

    @Override
    public void write(ModelDescriptor model, WriteContext ctx) {
        if (!Generator.isValidModelDescriptor(model, ctx)) {
            return;
        }
        ctx.registerInsomniaModel(model);
        generateInsomniaFile(ctx, ctx);
    }

    private void generateInsomniaFile(
            ModelRegistry modelRegistry, ProcessingEnvironmentProvider envProvider) {
        try {
            Path insomniaPath = resolveOutputDirectory(envProvider).resolve(INSOMNIA_FILE);
            Path parent = insomniaPath.getParent();
            if (parent == null) {
                throw new IOException("Output file has no parent directory: " + insomniaPath);
            }
            Files.createDirectories(parent);
            if (!writablePath.test(parent)) {
                throw new IOException("Output directory is not writable: " + parent);
            }
            String json = buildInsomniaJson(modelRegistry.insomniaModels());
            Files.writeString(
                    insomniaPath,
                    json,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            envProvider.env()
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.NOTE,
                            "Generated Insomnia collection at: " + insomniaPath);
        } catch (IOException e) {
            envProvider.env()
                    .getMessager()
                    .printMessage(
                            Diagnostic.Kind.WARNING,
                            "Failed to generate Insomnia file: " + e.getMessage());
        }
    }

    private Path resolveOutputDirectory(ProcessingEnvironmentProvider envProvider)
            throws IOException {
        if (outputDirectory != null) {
            return outputDirectory;
        }
        String configured = envProvider.env().getOptions().get(OUTPUT_DIR_OPTION);
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured).toAbsolutePath().normalize();
        }
        return Paths.get(
                        envProvider.env()
                                .getFiler()
                                .getResource(StandardLocation.CLASS_OUTPUT, "", INSOMNIA_FILE)
                                .toUri())
                .toAbsolutePath()
                .normalize()
                .getParent();
    }

    private String buildInsomniaJson(Set<ModelDescriptor> models) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"_type\": \"export\",\n");
        json.append("  \"__export_format\": 4,\n");
        json.append("  \"__export_date\": \"")
                .append(REPRODUCIBLE_EXPORT_DATE)
                .append("\",\n");
        json.append("  \"__export_source\": \"crudcraft-codegen\",\n");
        json.append("  \"resources\": [\n");

        List<String> resources = new ArrayList<>();
        resources.add(generateWorkspace());
        resources.add(generateBaseEnvironment(models));
        for (ModelDescriptor model : orderedUniqueModels(models)) {
            resources.addAll(InsomniaRequestResourceBuilder.generateModelResources(model));
        }

        json.append(String.join(",\n", resources));
        json.append("\n  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private String generateWorkspace() {
        return """
                {
                        "_id": "wrk_crudcraft",
                        "parentId": null,
                        "_type": "workspace",
                        "name": "CrudCraft Generated API",
                        "scope": "collection"
                }\
                """;
    }

    private String generateBaseEnvironment(Set<ModelDescriptor> models) {
        StringBuilder env = new StringBuilder();
        env.append("    {\n");
        env.append("      \"_id\": \"env_base\",\n");
        env.append("      \"parentId\": \"wrk_crudcraft\",\n");
        env.append("      \"_type\": \"environment\",\n");
        env.append("      \"name\": \"Base Environment\",\n");
        env.append("      \"data\": {\n");
        env.append("        \"base_url\": \"http://localhost:8080\",\n");
        env.append("        \"jwt_token\": \"\"");
        for (ModelDescriptor model : orderedUniqueModels(models)) {
            String varName = StringCase.SNAKE.apply(model.getName()) + "_id";
            env.append(",\n        \"").append(varName).append("\": \"\"");
        }
        env.append("\n      }\n");
        env.append("    }");
        return env.toString();
    }

    private List<ModelDescriptor> orderedUniqueModels(Set<ModelDescriptor> models) {
        TreeMap<String, ModelDescriptor> byQualifiedName = new TreeMap<>();
        for (ModelDescriptor model : models) {
            byQualifiedName.putIfAbsent(model.getPackageName() + "." + model.getName(), model);
        }
        return new ArrayList<>(byQualifiedName.values());
    }

    private List<String> generateModelResources(ModelDescriptor model) {
        return InsomniaRequestResourceBuilder.generateModelResources(model);
    }

    private Set<CrudEndpoint> resolveEndpoints(ModelDescriptor model) {
        return InsomniaRequestResourceBuilder.resolveEndpoints(model);
    }

    private boolean hasSearchFields(ModelDescriptor model) {
        return InsomniaRequestResourceBuilder.hasSearchFields(model);
    }

    private String generateFolder(String folderId, String name) {
        return InsomniaRequestResourceBuilder.generateFolder(folderId, name);
    }

    private String generateListRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateListRequest(model, folderId, endpoint, idVar);
    }

    private String generateGetRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateGetRequest(model, folderId, endpoint, idVar);
    }

    private String generateListRefRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        return InsomniaRequestResourceBuilder.generateListRefRequest(model, folderId, endpoint);
    }

    private String generateCreateRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateCreateRequest(
                model, folderId, endpoint, idVar);
    }

    private String generateUpdateRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateUpdateRequest(
                model, folderId, endpoint, idVar);
    }

    private String generatePatchRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generatePatchRequest(
                model, folderId, endpoint, idVar);
    }

    private String generateDeleteRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateDeleteRequest(
                model, folderId, endpoint, idVar);
    }

    private String generateBulkCreateRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        return InsomniaRequestResourceBuilder.generateBulkCreateRequest(model, folderId, endpoint);
    }

    private String generateBulkUpdateRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateBulkUpdateRequest(
                model, folderId, endpoint, idVar);
    }

    private String generateBulkPatchRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateBulkPatchRequest(
                model, folderId, endpoint, idVar);
    }

    private String generateBulkUpsertRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        return InsomniaRequestResourceBuilder.generateBulkUpsertRequest(model, folderId, endpoint);
    }

    private String generateFindByIdsRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateFindByIdsRequest(
                model, folderId, endpoint, idVar);
    }

    private String generateBulkDeleteRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateBulkDeleteRequest(
                model, folderId, endpoint, idVar);
    }

    private String generateExistsRequest(
            ModelDescriptor model, String folderId, String endpoint, String idVar) {
        return InsomniaRequestResourceBuilder.generateExistsRequest(
                model, folderId, endpoint, idVar);
    }

    private String generateCountRequest(ModelDescriptor model, String folderId, String endpoint) {
        return InsomniaRequestResourceBuilder.generateCountRequest(model, folderId, endpoint);
    }

    private String generateSearchRequest(ModelDescriptor model, String folderId, String endpoint) {
        return InsomniaRequestResourceBuilder.generateSearchRequest(model, folderId, endpoint);
    }

    private String generateValidateRequest(
            ModelDescriptor model, String folderId, String endpoint) {
        return InsomniaRequestResourceBuilder.generateValidateRequest(model, folderId, endpoint);
    }

    private String generateExportRequest(ModelDescriptor model, String folderId, String endpoint) {
        return InsomniaRequestResourceBuilder.generateExportRequest(model, folderId, endpoint);
    }

    private String generateSampleRequestBody(ModelDescriptor model) {
        return InsomniaPayloadHelper.generateSampleRequestBody(model);
    }

    private String getSampleValue(FieldDescriptor field) {
        return InsomniaPayloadHelper.getSampleValue(field);
    }

    private String escapeJson(String value) {
        return InsomniaPayloadHelper.escapeJson(value);
    }

    private String jsonFormat(String template, Object... args) {
        return InsomniaPayloadHelper.jsonFormat(template, args);
    }

    private String formatDisplayName(String name) {
        return InsomniaPayloadHelper.formatDisplayName(name);
    }
}
