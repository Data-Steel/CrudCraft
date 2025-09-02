CrudCraft – Rapid CRUD API Generator for Spring Boot Applications
CrudCraft is an innovative, modular toolkit that simplifies and accelerates the creation of CRUD-style REST APIs using Spring Boot and Hibernate. Designed specifically for developers who prefer to focus on business logic rather than repetitive boilerplate code, CrudCraft automates the tedious parts of backend API development through a concise, annotation-driven approach.

Core Idea
CrudCraft leverages Java annotations to automate code generation, dramatically reducing manual coding and configuration. Developers annotate their Java entities, and CrudCraft automatically generates fully functional API endpoints, DTOs, repositories, services, and controllers.

MVP Features
Annotation-Driven Code Generation
Define your data models with straightforward annotations (@CrudCrafted, @Dto, @Request).

Automatically generate standard RESTful CRUD endpoints.

Modular Architecture
Clearly separated modules for annotations, code generation, runtime utilities, and tooling.

Easily extendable with your own logic through editable stubs.

Compile-Time Generation
An annotation processor (CrudCraftProcessor) reads annotations and outputs boilerplate code during the build process.

Supports entities, DTOs, repository interfaces, services, mappers, controllers, and relationship management utilities.

Flexible & Editable Stubs
Generated stubs (editable = true) allow manual customization without risk of overwriting on regeneration.

Utilities (EditableFileTool) move customizable stubs into your source directory, ready for your custom logic.

Runtime Libraries
Abstract base classes (AbstractCrudService, AbstractCrudController) provide ready-to-use generic functionality.

Built-in exception handling and data mapping capabilities simplify backend logic further.

Relationship Management
Generated RelationshipMeta helpers keep entity relationships synchronized and consistent.

Simplifies bidirectional associations and nested data structures.

Easy Integration
Spring Boot starter provides auto-configuration for effortless setup.

Quickly embed CrudCraft within your existing Spring Boot projects without complex configuration.

Architecture Overview
CrudCraft maintains a clean separation between generated and editable code, utilizing a straightforward workflow:

Annotate your JPA entities.

Run a build process (mvn clean install), automatically generating API layers.

Customize generated stub classes safely and independently.

Run your Spring Boot application for an immediate, fully functional CRUD API.

Included Modules
crudcraft-codegen: Annotation definitions and processor handling code generation.

crudcraft-runtime: Runtime dependencies including abstract controllers/services.

crudcraft-starter: Spring Boot starter for zero-configuration setup.

crudcraft-tools: Utility classes managing editable file integration.

crudcraft-sample-app: Demonstration project illustrating CrudCraft’s features and setup.

Benefits of CrudCraft MVP
Rapid Development: Cuts down development time significantly by automating repetitive tasks.

Reduced Errors: Minimizes potential bugs through standardized generation and robust handling.

High Extensibility: Provides freedom for custom logic and business-specific extensions.

Maintainable Codebase: Keeps generated code isolated from custom logic, ensuring maintainability and clarity.

Ideal Use Cases
CrudCraft is ideal for projects that:

Require quick prototyping or MVPs.

Need structured APIs with clear and consistent endpoints.

Value clean architecture, maintainability, and extensibility.

Prefer convention-over-configuration and automation.

How to Get Started
Clone, annotate your entities, build your project, and immediately have a robust, fully featured API.

Vision & Roadmap
Future enhancements planned for CrudCraft include:

Advanced validation and security annotations.

Enhanced search, filtering, and pagination capabilities.

Integration with popular external services and tools (Elasticsearch, Kafka, Prometheus, etc.).

Expanded internationalization and multi-tenancy support.

CrudCraft's MVP provides a robust, extensible foundation for Spring Boot developers, streamlining API development while preserving flexibility and scalability for real-world applications.