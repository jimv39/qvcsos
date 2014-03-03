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

import java.util.List;

/**
 * Holds a delete-delta between to revisions of a text.
 *
 * @version $Id: DeleteDelta.java,v 1.6 2004/02/28 03:35:36 bayard Exp $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @see Delta
 * @see Diff
 * @see Chunk
 */
public class DeleteDelta
    extends Delta
{

    DeleteDelta()
    {
        super();
    }

    public DeleteDelta(Chunk orig)
    {
        init(orig, null);
    }

    public void verify(List target)
        throws PatchFailedException
    {
        if (!original.verify(target))
        {
            throw new PatchFailedException();
        }
    }

    public void applyTo(List target)
    {
        original.applyDelete(target);
    }

    public void toString(StringBuffer s)
    {
        s.append(original.rangeString());
        s.append("d");
        s.append(revised.rcsto());
        s.append(Diff.NL);
        original.toString(s, "< ", Diff.NL);
    }

    public void toRCSString(StringBuffer s, String EOL)
    {
        s.append("d");
        s.append(original.rcsfrom());
        s.append(" ");
        s.append(original.size());
        s.append(EOL);
    }

    public void accept(RevisionVisitor visitor)
    {
        visitor.visit(this);
    }
}