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
 * Holds a "delta" difference between to revisions of a text.
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/28 03:35:36 $
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @author <a href="mailto:bwm@hplb.hpl.hp.com">Brian McBride</a>
 * @see Diff
 * @see Chunk
 * @see Revision
 *
 * modifications
 *
 * 27 Apr 2003 bwm
 *
 * Added getOriginal() and getRevised() accessor methods
 * Added visitor pattern accept() method
 */

public abstract class Delta
    extends org.apache.commons.jrcs.util.ToString
{

    protected Chunk original;

    protected Chunk revised;

    static Class[][] DeltaClass;

    static
    {
        DeltaClass = new Class[2][2];
        try
        {
            DeltaClass[0][0] = org.apache.commons.jrcs.diff.ChangeDelta.class;
            DeltaClass[0][1] = org.apache.commons.jrcs.diff.AddDelta.class;
            DeltaClass[1][0] = org.apache.commons.jrcs.diff.DeleteDelta.class;
            DeltaClass[1][1] = org.apache.commons.jrcs.diff.ChangeDelta.class;
        }
        catch (Throwable o)
        {

        }
    }

    /**
     * Returns a Delta that corresponds to the given chunks in the
     * original and revised text respectively.
     * @param orig the chunk in the original text.
     * @param rev  the chunk in the revised text.
     */
    public static Delta newDelta(Chunk orig, Chunk rev)
    {
        Class c = DeltaClass[orig.size() > 0 ? 1 : 0]
            [rev.size() > 0 ? 1 : 0];
        Delta result;
        try
        {
            result = (Delta) c.newInstance();
        }
        catch (Throwable e)
        {
            return null;
        }
        result.init(orig, rev);
        return result;
    }

    /**
     * Creates an uninitialized delta.
     */
    protected Delta()
    {
    }

    /**
     * Creates a delta object with the given chunks from the original
     * and revised texts.
     */
    protected Delta(Chunk orig, Chunk rev)
    {
        init(orig, rev);
    }

    /**
     * Initializaes the delta with the given chunks from the original
     * and revised texts.
     */
    protected void init(Chunk orig, Chunk rev)
    {
        original = orig;
        revised = rev;
    }

    /**
     * Verifies that this delta can be used to patch the given text.
     * @param target the text to patch.
     * @throws PatchFailedException if the patch cannot be applied.
     */
    public abstract void verify(List target)
        throws PatchFailedException;

    /**
     * Applies this delta as a patch to the given text.
     * @param target the text to patch.
     * @throws PatchFailedException if the patch cannot be applied.
     */
    public final void patch(List target)
        throws PatchFailedException
    {
        verify(target);
        try
        {
            applyTo(target);
        }
        catch (Exception e)
        {
            throw new PatchFailedException(e.getMessage());
        }
    }

    /**
     * Applies this delta as a patch to the given text.
     * @param target the text to patch.
     * @throws PatchFailedException if the patch cannot be applied.
     */
    public abstract void applyTo(List target);

    /**
     * Converts this delta into its Unix diff style string representation.
     * @param s a {@link StringBuffer StringBuffer} to which the string
     * representation will be appended.
     */
    public void toString(StringBuffer s)
    {
        original.rangeString(s);
        s.append("x");
        revised.rangeString(s);
        s.append(Diff.NL);
        original.toString(s, "> ", "\n");
        s.append("---");
        s.append(Diff.NL);
        revised.toString(s, "< ", "\n");
    }

    /**
     * Converts this delta into its RCS style string representation.
     * @param s a {@link StringBuffer StringBuffer} to which the string
     * representation will be appended.
     * @param EOL the string to use as line separator.
     */
    public abstract void toRCSString(StringBuffer s, String EOL);

    /**
     * Converts this delta into its RCS style string representation.
     * @param EOL the string to use as line separator.
     */
    public String toRCSString(String EOL)
    {
        StringBuffer s = new StringBuffer();
        toRCSString(s, EOL);
        return s.toString();
    }

    /**
     * Accessor method to return the chunk representing the original
     * sequence of items
     *
     * @return the original sequence
     */
    public Chunk getOriginal()
    {
        return original;
    }

    /**
     * Accessor method to return the chunk representing the updated
     * sequence of items.
     *
     * @return the updated sequence
     */
    public Chunk getRevised()
    {
        return revised;
    }

    /**
     * Accepts a visitor.
     * <p>
     * See the Visitor pattern in "Design Patterns" by the GOF4.
     * @param visitor The visitor.
     */
    public abstract void accept(RevisionVisitor visitor);
}
