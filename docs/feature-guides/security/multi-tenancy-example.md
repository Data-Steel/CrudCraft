# Multi-Tenancy Example

CrudCraft tenant isolation is configured with `@TenantScoped` or claim-scoped row handlers.

1. Configure Spring Security to authenticate JWT/OAuth2 tokens.
2. Ensure the authentication contains a tenant claim, for example `tenant_id`.
3. Annotate the entity with the matching row-scope annotation.
4. Use generated services normally; read/search/count/export paths receive the tenant filter through runtime extensions.

Example:

```java
@CrudCrafted(secure = true)
@TenantScoped(claim = "tenant_id", field = "tenantId")
class Invoice {
    @Id UUID id;
    String tenantId;
}
```

When the claim is missing, CrudCraft returns no rows for scoped reads. Attempts to access data outside the caller tenant should be logged by the application security/audit layer as an authorization miss, not as a missing entity leak.
