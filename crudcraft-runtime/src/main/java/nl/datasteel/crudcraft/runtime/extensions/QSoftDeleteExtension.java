/*
 * Copyright (c) 2025 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.datasteel.crudcraft.runtime.extensions;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import static com.querydsl.core.types.PathMetadataFactory.forVariable;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.DateTimePath;
import jakarta.annotation.Generated;
import java.io.Serial;
import java.time.Instant;

@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QSoftDeleteExtension extends BeanPath<SoftDeleteExtension> {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final QSoftDeleteExtension softDeleteExtension = new QSoftDeleteExtension("softDeleteExtension");

    public final BooleanPath deleted = createBoolean("deleted");

    public final DateTimePath<Instant> deletedAt = createDateTime("deletedAt", Instant.class);

    public QSoftDeleteExtension(String variable) {
        super(SoftDeleteExtension.class, forVariable(variable));
    }

    public QSoftDeleteExtension(Path<? extends SoftDeleteExtension> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSoftDeleteExtension(PathMetadata metadata) {
        super(SoftDeleteExtension.class, metadata);
    }
}