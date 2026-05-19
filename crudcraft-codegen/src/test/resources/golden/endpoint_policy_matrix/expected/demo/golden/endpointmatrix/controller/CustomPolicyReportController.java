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
package demo.golden.endpointmatrix.controller;

import demo.golden.endpointmatrix.CustomPolicyReport;
import demo.golden.endpointmatrix.dto.request.CustomPolicyReportRequestDto;
import demo.golden.endpointmatrix.dto.response.CustomPolicyReportResponseDto;
import demo.golden.endpointmatrix.service.CustomPolicyReportService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import nl.datasteel.crudcraft.runtime.export.ExportRequest;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportService;
import nl.datasteel.crudcraft.runtime.export.service.EnhancedExportServiceFactory;
import nl.datasteel.crudcraft.runtime.export.service.ExportService;
import nl.datasteel.crudcraft.runtime.service.CrudQueryOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * Generated model file for CustomPolicyReport; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: CustomPolicyReport
 * - Package: demo.golden.endpointmatrix.controller
 * - Generator: ControllerGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@RestController
@RequestMapping("/custompolicyreports")
public class CustomPolicyReportController {
    private static final Logger LOG = LoggerFactory.getLogger(CustomPolicyReportController.class);

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    private final CustomPolicyReportService service;

    private final MeterRegistry meterRegistry;

    protected int maxPageSize;

    protected int maxRows;

    protected int maxCsvRows;

    protected int maxJsonRows;

    protected int maxXlsxRows;

    protected int maxDepth;

    private final EnhancedExportService<CustomPolicyReportResponseDto, CustomPolicyReport, Object> exportService;

    @SuppressFBWarnings(
            value = "CT_CONSTRUCTOR_THROW",
            justification = "Generated controllers may validate optional infrastructure during construction."
    )
    public CustomPolicyReportController(CustomPolicyReportService service,
            ObjectProvider<MeterRegistry> meterRegistry,
            @Value("${crudcraft.api.max-page-size:100}") int maxPageSize,
            @Value("${crudcraft.export.max-rows:-1}") int maxRows,
            @Value("${crudcraft.export.max-csv-rows:100000}") int maxCsvRows,
            @Value("${crudcraft.export.max-json-rows:50000}") int maxJsonRows,
            @Value("${crudcraft.export.max-xlsx-rows:25000}") int maxXlsxRows,
            @Value("${crudcraft.export.max-depth:5}") int maxDepth,
            ObjectProvider<EnhancedExportServiceFactory> exportServiceFactoryProvider) {
        if (maxPageSize <= 0) {
            throw new IllegalArgumentException("crudcraft.api.max-page-size must be positive; got " + maxPageSize);
        }
        this.service = service;
        this.meterRegistry = resolveMeterRegistry(meterRegistry);
        this.maxPageSize = maxPageSize;
        this.maxRows = maxRows;
        this.maxCsvRows = maxCsvRows;
        this.maxJsonRows = maxJsonRows;
        this.maxXlsxRows = maxXlsxRows;
        this.maxDepth = maxDepth;
        this.exportService = createExportService(exportServiceFactoryProvider, maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, CustomPolicyReport.class);
    }

    private static MeterRegistry resolveMeterRegistry(ObjectProvider<MeterRegistry> provider) {
        if (provider == null) {
            return null;
        }
        try {
            return provider.getIfAvailable();
        } catch (RuntimeException ex) {
            LOG.debug("Micrometer registry lookup failed; generated controller metrics disabled", ex);
            return null;
        }
    }

    private Pageable clampPageable(Pageable pageable) {
        if (pageable == null) {
            return PageRequest.of(0, maxPageSize);
        }
        int size = Math.clamp(pageable.getPageSize(), 1, maxPageSize);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }

    private void recordOperation(String operation, String outcome, long started) {
        long durationNanos = System.nanoTime() - started;
        LOG.info("crudcraft.generated.operation model={} operation={} outcome={} duration_ms={}", "CustomPolicyReport", operation, outcome, TimeUnit.NANOSECONDS.toMillis(durationNanos));
        if (meterRegistry != null) {
            Timer.builder("crudcraft.generated.operation").tag("model", "CustomPolicyReport").tag("operation", operation).tag("outcome", outcome).register(meterRegistry).record(durationNanos, TimeUnit.NANOSECONDS);
        }
    }

    @SuppressWarnings("unchecked")
    private static EnhancedExportService<CustomPolicyReportResponseDto, CustomPolicyReport, Object> createExportService(
            ObjectProvider<EnhancedExportServiceFactory> provider, int maxRows, int maxCsvRows,
            int maxJsonRows, int maxXlsxRows, int maxPageSize,
            Class<CustomPolicyReport> entityType) {
        if (provider != null) {
            EnhancedExportServiceFactory factory = provider.getIfAvailable();
            if (factory != null) {
                return factory.create(maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize, entityType);
            }
        }
        return new EnhancedExportService<>(new ExportService.ExportConfig(maxRows, maxCsvRows, maxJsonRows, maxXlsxRows, maxPageSize), null, entityType, false);
    }

    @SuppressWarnings("unchecked")
    private Specification<CustomPolicyReport> effectiveReadSpecification(Object searchRequest) {
        return ((CrudQueryOperations<CustomPolicyReport, ?, ?>) service).effectiveReadSpecification(searchRequest);
    }

    @GetMapping("/{id}")
    @Operation(
            operationId = "customPolicyReportGetOne",
            summary = "Get a single CustomPolicyReport by ID",
            description = "Retrieves a single CustomPolicyReport entity by its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CustomPolicyReport retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "CustomPolicyReport with the specified ID was not found")
    })
    public ResponseEntity<CustomPolicyReportResponseDto> getOne(@PathVariable UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            CustomPolicyReportResponseDto dto = service.findById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("GET_ONE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @RequestMapping(
            value = "/exists/{id}",
            method = {RequestMethod.HEAD, RequestMethod.GET}
    )
    @Operation(
            operationId = "customPolicyReportExists",
            summary = "Check if a CustomPolicyReport exists",
            description = "Checks whether a CustomPolicyReport entity with the given ID exists in the system."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "CustomPolicyReport existence check result"),
            @ApiResponse(responseCode = "404", description = "CustomPolicyReport with the specified ID was not found")
    })
    public ResponseEntity<Void> exists(@PathVariable("id") UUID id) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            return service.existsById(id)
                    ? ResponseEntity.ok().build()
                    : ResponseEntity.notFound().build();
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("EXISTS", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/count")
    @Operation(
            operationId = "customPolicyReportCount",
            summary = "Count CustomPolicyReport entities",
            description = "Counts the total number of CustomPolicyReport entities matching the search criteria."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Count of CustomPolicyReport entities"))
    public ResponseEntity<Map<String, Long>> count() {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            long total = service.count();
            return ResponseEntity.ok(Map.of("count", total));
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("COUNT", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @PostMapping("/validate")
    @Operation(
            operationId = "customPolicyReportValidate",
            summary = "Validate CustomPolicyReport data",
            description = "Validates CustomPolicyReport data without persisting it. Returns validation errors if any."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Validation results"))
    public ResponseEntity<Void> validate(
            @Valid @NotNull @RequestBody CustomPolicyReportRequestDto request) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("VALIDATE", _crudcraftOutcome, _crudcraftStarted);
        }
    }

    @GetMapping("/export")
    @Operation(
            operationId = "customPolicyReportExport",
            summary = "Export CustomPolicyReport entities",
            description = "Exports CustomPolicyReport entities in the requested format."
    )
    @ApiResponses(@ApiResponse(responseCode = "200", description = "CustomPolicyReport data exported"))
    public ResponseEntity<StreamingResponseBody> export(
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "format", required = true) String format,
            @ModelAttribute ExportRequest exportRequest) {
        long _crudcraftStarted = System.nanoTime();
        String _crudcraftOutcome = "success";
        try {
            return exportService.export(
                null,
                limit,
                format,
                exportRequest,
                pageable -> service.findAll(pageable),
                Function.identity(),
                effectiveReadSpecification(null)
            );
        } catch (RuntimeException ex) {
            _crudcraftOutcome = "error";
            throw ex;
        } finally {
            recordOperation("EXPORT", _crudcraftOutcome, _crudcraftStarted);
        }
    }
}
