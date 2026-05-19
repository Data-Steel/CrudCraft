# Endpoint Template Matrix

| Template | List | Ref | Get | Create | Put | Patch | Delete | Bulk | Search | Exists | Count | Validate |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| `FULL` | yes | yes | yes | yes | yes | yes | yes | yes | when searchable | yes | yes | yes |
| `READ_ONLY` | yes | yes | yes | no | no | no | no | find-by-ids only | when searchable | yes | yes | no |
| `IMMUTABLE_WRITE` | yes | yes | yes | yes | no | no | no | create only | when searchable | yes | yes | no |
| `PATCH_ONLY` | yes | no | yes | no | no | yes | no | patch only | when searchable | yes | yes | no |
| `NO_DELETE` | yes | yes | yes | yes | yes | yes | no | create/update/patch/upsert | when searchable | yes | yes | yes |
| `NO_BATCH` | yes | yes | yes | yes | yes | yes | yes | no | when searchable | yes | yes | yes |
| `CREATE_ONLY` | no | no | no | yes | no | no | no | create/upsert | no | no | no | no |
| `SEARCH_ONLY` | no | no | no | no | no | no | no | no | yes | no | no | no |
| `META_ONLY` | no | no | no | no | no | no | no | no | no | yes | yes | no |
| `LIGHT_PUBLIC` | no | yes | yes | no | no | no | no | no | no | no | no | no |
| `SECURE_INTERNAL` | yes | yes | yes | yes | yes | yes | yes | no | when searchable | yes | yes | yes |
| `VALIDATION_ONLY` | no | no | no | no | no | no | no | no | no | no | no | yes |

`secure=true` applies endpoint authorization to every generated endpoint in the selected template. Field-level security is independent and applies whenever field security metadata is generated.
