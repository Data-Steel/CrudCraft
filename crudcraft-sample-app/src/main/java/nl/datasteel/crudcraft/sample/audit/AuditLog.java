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

package nl.datasteel.crudcraft.sample.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.CrudEndpoint;
import nl.datasteel.crudcraft.annotations.classes.CrudCrafted;
import nl.datasteel.crudcraft.annotations.fields.Dto;
import nl.datasteel.crudcraft.sample.security.AuditLogSecurityPolicy;

/**
 * Audit log entry created by hooks.
 * Demonstrates read-only resources with a custom security policy
 * that restricts access to administrators and auditors.
 */
@CrudCrafted(editable = false, omitEndpoints = {CrudEndpoint.POST, CrudEndpoint.PUT,
        CrudEndpoint.PATCH, CrudEndpoint.DELETE}, securityPolicy = AuditLogSecurityPolicy.class)
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    /**
     * Identifier of the log entry. Generated UUID ensures uniqueness and is
     * returned to clients for traceability.
     */
    @Dto
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Time when the audited action occurred. Always present and exposed
     * read-only in the DTO.
     */
    @Dto
    @Column(nullable = false)
    private OffsetDateTime timestamp;

    /**
     * Optional reference to the user that performed the action. Included so
     * consumers can correlate logs with actors.
     */
    @Dto
    @Column(name = "actor_user_id")
    private UUID actorUserId;

    /**
     * Description of the action (e.g. CREATE, UPDATE). Stored as free text.
     */
    @Dto
    private String action;

    /**
     * Type of the subject entity being audited, such as "Account".
     */
    @Dto
    private String subjectType;

    /**
     * Identifier of the subject entity to uniquely locate it later.
     */
    @Dto
    private String subjectId;

    /**
     * JSON snapshot of the entity before the change. Large payloads stored in
     * a text column.
     */
    @Dto
    @Column(columnDefinition = "TEXT")
    private String beforeJson;

    /**
     * JSON snapshot after the change, allowing clients to diff values.
     */
    @Dto
    @Column(columnDefinition = "TEXT")
    private String afterJson;

    /**
     * Correlation identifier so multiple audit logs can be tied to a single
     * request or workflow.
     */
    @Dto
    @Column(name = "correlation_id")
    private String correlationId;
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(UUID actorUserId) {
        this.actorUserId = actorUserId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public String getBeforeJson() {
        return beforeJson;
    }

    public void setBeforeJson(String beforeJson) {
        this.beforeJson = beforeJson;
    }

    public String getAfterJson() {
        return afterJson;
    }

    public void setAfterJson(String afterJson) {
        this.afterJson = afterJson;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
