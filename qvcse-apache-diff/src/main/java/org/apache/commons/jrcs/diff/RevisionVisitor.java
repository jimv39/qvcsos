/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.jrcs.diff;

/**
 * Definition of a Visitor interface for {@link Revision Revisions} See "Design Patterns" by the Gang of Four
 */
public interface RevisionVisitor {

    void visit(Revision revision);

    void visit(DeleteDelta delta);

    void visit(ChangeDelta delta);

    void visit(AddDelta delta);
}
