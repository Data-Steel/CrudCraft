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

import nl.datasteel.crudcraft.annotations.security.RowPredicate;
import nl.datasteel.crudcraft.annotations.security.RowSecurityHandler;
import nl.datasteel.crudcraft.runtime.exception.ForbiddenException;
import nl.datasteel.crudcraft.sample.blog.Post;
import nl.datasteel.crudcraft.sample.blog.PostStatus;


/** Sample row-security handler that hides archived posts from non-administrative flows. */
public final class PostRowSecurityHandler implements RowSecurityHandler<Post> {

    @Override
    public RowPredicate<Post> rowFilter() {
        return (root, query, cb) -> cb.notEqual(root.get("status"), PostStatus.ARCHIVED);
    }

    @Override
    public void apply(Post entity) {
        if (entity.getStatus() == PostStatus.ARCHIVED) {
            throw new ForbiddenException("Archived posts are read-only in the sample policy.");
        }
    }
}
