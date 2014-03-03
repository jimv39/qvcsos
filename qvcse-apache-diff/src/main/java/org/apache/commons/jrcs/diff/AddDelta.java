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
 * Holds an add-delta between to revisions of a text.
 *
 * @version $Id: AddDelta.java,v 1.5 2004/02/28 03:35:36 bayard Exp $
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @see Delta
 * @see Diff
 * @see Chunk
 */
public class AddDelta
    extends Delta
{

    AddDelta()
    {
        super();
    }

    public AddDelta(int origpos, Chunk rev)
    {
        init(new Chunk(origpos, 0), rev);
    }

    public void verify(List target) throws PatchFailedException
    {
        if (original.first() > target.size())
        {
            throw new PatchFailedException("original.first() > target.size()");
        }
    }

    public void applyTo(List target)
    {
        revised.applyAdd(original.first(), target);
    }

    public void toString(StringBuffer s)
    {
        s.append(original.anchor());
        s.append("a");
        s.append(revised.rangeString());
        s.append(Diff.NL);
        revised.toString(s, "> ", Diff.NL);
    }

    public void toRCSString(StringBuffer s, String EOL)
    {
        s.append("a");
        s.append(original.anchor());
        s.append(" ");
        s.append(revised.size());
        s.append(EOL);
        revised.toString(s, "", EOL);
    }

    public void Accept(RevisionVisitor visitor)
    {
        visitor.visit(this);
    }

    public void accept(RevisionVisitor visitor)
    {
        visitor.visit(this);
    }
}





