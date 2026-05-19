---
title: "Security Authentication"
description: "Understand what authentication state CrudCraft security expects from a Spring Boot application."
section: "Feature Guides"
category: "Security"
audience:
  - "Application developers"
status: "stable"
crudcraft_modules:
  - "crudcraft-runtime-security"
related:
  - "/feature-guides/security/configuration"
  - "/feature-guides/security/tenant-isolation"
---

# Security Authentication

CrudCraft does not authenticate users. It consumes Spring Security's current `Authentication` to evaluate generated `@PreAuthorize`, field roles, and row-scope claims.

Use this page when wiring JWT, session, test authentication, or custom principals for generated APIs.

## Required contract

CrudCraft reads authentication from `SecurityContextHolder`. The principal must provide:

| Needed for | Source |
|---|---|
| Endpoint `@PreAuthorize` | Spring Security method security. |
| `@FieldSecurity` roles | `Authentication.getAuthorities()`, with optional `ROLE_` prefix normalization. |
| `@TenantScoped`, `@ClientScoped`, `@OwnedBy` claims | principal, details, credentials, claims map, `getClaim`, `getClaimAsString`, `getClaims`, or `Authentication.getName()` for `sub`. |

## JWT-like principal example

```java
Authentication authentication = new TestingAuthenticationToken(
    Map.of("tenant_id", "tenant-a", "client_id", "client-42", "sub", "user-9"),
    "token",
    "ROLE_USER"
);
authentication.setAuthenticated(true);
SecurityContextHolder.getContext().setAuthentication(authentication);
```

This gives CrudCraft enough data for `@CrudSecurity(readRoles = "USER")`, `@TenantScoped(claim = "tenant_id")`, `@ClientScoped(claim = "client_id")`, and `@OwnedBy(claim = "sub")`.

## Application example

```java
@Configuration
@EnableMethodSecurity
class SecurityConfiguration {
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
            .build();
    }
}
```

The important CrudCraft part is `@EnableMethodSecurity`; generated endpoints use method annotations. The rest depends on your authentication design.

## Related documentation

- [Security Configuration](configuration.md)
- [Tenant Isolation](tenant-isolation.md)
