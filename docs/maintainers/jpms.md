# JPMS Compatibility

CrudCraft publishes explicit `module-info.java` descriptors and `Automatic-Module-Name` metadata.

## Compatibility Rules

1. Exported package names are part of the compatibility surface.
2. Module names must stay stable across patch and minor releases.
3. New exports are allowed in minor releases; removing exports requires a major release.
4. Internal implementation packages should stay unexported unless explicitly documented as SPI.

## Maintainer Checks

1. Keep Maven dependencies and `module-info.java` `requires` aligned.
2. Avoid split packages across modules.
3. Keep starter automatic module names aligned with declared module names.
4. Run JPMS compile checks from CI (`ci.yml` `jpms` job) for boundary validation.
