# Mapper Customization

CrudCraft generates MapStruct mappers plus editable stubs for user logic.

Use MapStruct converters for type changes:

```java
@Mapper(componentModel = "spring")
public interface MoneyMapper {
    String toString(Money value);
    Money fromString(String value);
}
```

Reference custom mappers from the editable mapper stub rather than editing generated files. Override whole-object hooks only when the default field mapping is insufficient. Keep generated method signatures intact because services call the `EntityMapper<T, U, R, F, ID>` contract.

For whole-object adjustments that should survive mapper regeneration, override the generated
service's mapper customization hook and return an `EntityMapperCustomizer<T, U, R, F>`.
The runtime invokes it after `fromRequest`, `update`, `patch`, `toResponse`, and `toRef`.

Prefer nested mapping methods for relationship-specific customization. For security-sensitive fields, combine mapper customization with field-level security so redaction remains enforced after mapping.
