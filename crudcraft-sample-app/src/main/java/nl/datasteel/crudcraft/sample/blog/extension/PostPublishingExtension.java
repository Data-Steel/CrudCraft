/*
 * Copyright (c) 2026 CrudCraft contributors
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

package nl.datasteel.crudcraft.sample.blog.extension;

import java.time.OffsetDateTime;
import nl.datasteel.crudcraft.runtime.service.extension.CrudRuntimeExtension;
import nl.datasteel.crudcraft.sample.blog.Post;
import nl.datasteel.crudcraft.sample.blog.PostStatus;


/** Sample service extension that stamps first publication time before persistence. */
public final class PostPublishingExtension implements CrudRuntimeExtension<Post, Object> {

    @Override
    public void beforeSave(Post entity) {
        if (entity.getStatus() == PostStatus.PUBLISHED && entity.getPublishedAt() == null) {
            entity.setPublishedAt(OffsetDateTime.now());
        }
    }
}
