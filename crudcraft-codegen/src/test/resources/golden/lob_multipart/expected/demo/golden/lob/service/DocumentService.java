package demo.golden.lob.service;

import demo.golden.lob.Document;
import demo.golden.lob.dto.ref.DocumentRef;
import demo.golden.lob.dto.request.DocumentRequestDto;
import demo.golden.lob.dto.response.DocumentResponseDto;
import demo.golden.lob.mapper.DocumentMapper;
import demo.golden.lob.meta.DocumentRelationshipMeta;
import demo.golden.lob.repository.DocumentRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for Document; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Document
 * - Package: demo.golden.lob.service
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
@SuppressWarnings("PMD")
@Service
public class DocumentService extends AbstractCrudService<Document, DocumentRequestDto, DocumentResponseDto, DocumentRef, UUID> {
    public DocumentService(DocumentRepository repository, DocumentMapper mapper) {
        super(repository, mapper, Document.class, DocumentResponseDto.class, DocumentRef.class);
    }

    @Override
    protected void postSave(Document entity) {
        DocumentRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Document entity) {
        DocumentRelationshipMeta.clear(entity);
    }
}
