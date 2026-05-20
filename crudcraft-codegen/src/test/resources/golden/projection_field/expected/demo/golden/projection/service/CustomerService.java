package demo.golden.projection.service;

import demo.golden.projection.Customer;
import demo.golden.projection.dto.ref.CustomerRef;
import demo.golden.projection.dto.request.CustomerRequestDto;
import demo.golden.projection.dto.response.CustomerResponseDto;
import demo.golden.projection.mapper.CustomerMapper;
import demo.golden.projection.meta.CustomerRelationshipMeta;
import demo.golden.projection.repository.CustomerRepository;
import java.util.UUID;
import nl.datasteel.crudcraft.runtime.service.AbstractCrudService;
import org.springframework.stereotype.Service;

/**
 * Generated model file for Customer; do not edit manually.
 * @CrudCraft:generated
 *
 * This class contains CrudCraft's default implementation for this layer.
 *
 * Features:
 * - Complete CRUD handling
 * - Ready-to-use repository integration
 *
 * Generation context:
 * - Source model: Customer
 * - Package: demo.golden.projection.service
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
public class CustomerService extends AbstractCrudService<Customer, CustomerRequestDto, CustomerResponseDto, CustomerRef, UUID> {
    public CustomerService(CustomerRepository repository, CustomerMapper mapper) {
        super(repository, mapper, Customer.class, CustomerResponseDto.class, CustomerRef.class);
    }

    @Override
    protected void postSave(Customer entity) {
        CustomerRelationshipMeta.fix(entity);
    }

    @Override
    protected void preDelete(Customer entity) {
        CustomerRelationshipMeta.clear(entity);
    }
}
