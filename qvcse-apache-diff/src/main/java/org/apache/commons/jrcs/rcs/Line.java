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

package org.apache.commons.jrcs.rcs;

/**
 * An annotated line of a revision.
 * Line contains both the original text of the line, plus the node
 * that indicates the revision in which the line was last added or changed.
 * This class is NOT thread safe.
 *
 * @see Node
 * @see Archive
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @version $Id: Line.java,v 1.4 2004/02/28 03:35:36 bayard Exp $
 */
final public class Line
{
    private final Node revision;
    private final Object text;

    Line(Node revision, Object text)
    {
        this.text = text;
        this.revision = revision;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof Line))
        {
            return false;
        }
        else
        {
            return this.getText().equals(((Line) other).getText());
        }
    }

    public int hashCode()
    {
        return getText().hashCode();
    }

    final Node getRevision()
    {
        return revision;
    }

    final Object getText()
    {
        return text;
    }
}
