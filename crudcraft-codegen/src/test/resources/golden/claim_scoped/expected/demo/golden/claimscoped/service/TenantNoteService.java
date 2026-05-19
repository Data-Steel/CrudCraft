package demo.golden.claimscoped.service;

import demo.golden.claimscoped.TenantNote;
import demo.golden.claimscoped.dto.ref.TenantNoteRef;
import demo.golden.claimscoped.dto.request.TenantNoteRequestDto;
import demo.golden.claimscoped.dto.response.TenantNoteResponseDto;
import demo.golden.claimscoped.mapper.TenantNoteMapper;
import demo.golden.claimscoped.meta.TenantNoteRelationshipMeta;
import demo.golden.claimscoped.repository.TenantNoteRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.row.ClaimScopedRowSecurityHandler;
import nl.datasteel.crudcraft.runtime.security.row.RowSecurityRuntimeExtension;
import nl.datasteel.crudcraft.runtime.security.scope.PrincipalScopeAccessor;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import org.springframework.stereotype.Service;

/**
 * Generated model file for TenantNote; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: TenantNote
 * - Package: demo.golden.claimscoped.service
 * - Generator: ServiceGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * To make changes, edit the entity model class and rebuild the project.
 * Do not edit or rename this file manually.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@Service
public class TenantNoteService extends AbstractCrudService<TenantNote, TenantNoteRequestDto, TenantNoteResponseDto, TenantNoteRef, UUID> {
    private final List<CrudRuntimeExtension<TenantNote, TenantNoteRequestDto>> runtimeExtensions;

    public TenantNoteService(TenantNoteRepository repository, TenantNoteMapper mapper,
            PrincipalScopeAccessor principalScopeAccessor) {
        super(repository, mapper, TenantNote.class, TenantNoteResponseDto.class, TenantNoteRef.class);
        List<RowSecurityHandler<TenantNote>> rowSecurityHandlerList = new ArrayList<>();
        rowSecurityHandlerList.add(new ClaimScopedRowSecurityHandler<>("tenant", "tenantId", "tenant", principalScopeAccessor));
        rowSecurityHandlerList.add(new ClaimScopedRowSecurityHandler<>("owner", "ownerId", "subject", principalScopeAccessor));
        this.runtimeExtensions = List.of(new RowSecurityRuntimeExtension<>(rowSecurityHandlerList));
    }

    @Override
    protected void postSave(TenantNote entity) {
        TenantNoteRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(TenantNote entity) {
        TenantNoteRelationshipMeta.clear(entity);
    }

    @Override
    protected List<CrudRuntimeExtension<TenantNote, TenantNoteRequestDto>> runtimeExtensions() {
        return runtimeExtensions;
    }
}
