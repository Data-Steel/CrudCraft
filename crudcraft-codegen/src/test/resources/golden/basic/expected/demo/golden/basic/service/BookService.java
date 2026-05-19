package demo.golden.basic.service;

import demo.golden.basic.Book;
import demo.golden.basic.dto.ref.BookRef;
import demo.golden.basic.dto.request.BookRequestDto;
import demo.golden.basic.dto.response.BookResponseDto;
import demo.golden.basic.mapper.BookMapper;
import demo.golden.basic.meta.BookRelationshipMeta;
import demo.golden.basic.repository.BookRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for Book; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Book
 * - Package: demo.golden.basic.service
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
public class BookService extends AbstractCrudService<Book, BookRequestDto, BookResponseDto, BookRef, UUID> {
    public BookService(BookRepository repository, BookMapper mapper) {
        super(repository, mapper, Book.class, BookResponseDto.class, BookRef.class);
    }

    @Override
    protected void postSave(Book entity) {
        BookRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Book entity) {
        BookRelationshipMeta.clear(entity);
    }
}
