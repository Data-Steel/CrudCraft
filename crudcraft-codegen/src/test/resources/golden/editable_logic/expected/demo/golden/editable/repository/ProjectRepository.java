package demo.golden.editable.repository;

import demo.golden.editable.Project;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Generated Repository layer stub for Project.
 * @CrudCraft:generated
 * @CrudCraft:editable
 *
 * This Repository stub extends CrudCraft's base implementation. Override protected hooks and add custom endpoints here. Avoid overriding generated public endpoint methods unless you intentionally replace the HTTP contract.
 *
 * You are allowed to modify this file. It extends CrudCraft's abstract base (ProjectRepositoryBase)
 * which already implements full CRUD logic.
 *
 * This file was generated only once. CrudCraft will not overwrite it in future
 * builds. If you delete it, it will be regenerated.
 *
 * Features provided by CrudCraft:
 * - Standard CRUD workflow already implemented
 * - DTO mapping and repository calls wired up
 *
 * Generation context:
 * - Source model: Project
 * - Package: demo.golden.editable.repository
 * - Generator: RepositoryGenerator
 * - Generation time: 2026-01-01T00:00:00Z
 * - CrudCraft version: unknown
 *
 * Recommendations:
 * - You may customize method behavior, add validation, or extend with additional endpoints.
 * - Signature changes are allowed, but may desync from service or mapper layer—proceed with care.
 * - Do not manually copy or paste other CrudCraft stubs into this class.
 *
 * Support: For file generation bugs or feedback, open an issue at
 * https://github.com/Data-Steel/CrudCraft/issues.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID>, JpaSpecificationExecutor<Project> {
}
