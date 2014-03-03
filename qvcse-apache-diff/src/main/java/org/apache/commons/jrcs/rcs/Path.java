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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.jrcs.diff.PatchFailedException;

/**
 * A path from the head revision to a given revision in an Archive.
 * Path collaborates with Node in applying the set of deltas contained
 * in archive nodes to arrive at the text of the revision corresponding
 * to the last node in the path.
 * This class is NOT thread safe.
 *
 * @see Archive
 * @see Node
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @version $Id: Path.java,v 1.5 2004/02/28 03:35:36 bayard Exp $
 */
class Path
{
    private List path = new LinkedList();

    /**
     * Creates an empty Path
     */
    public Path()
    {
    }

    /**
     * Add a node to the Path.
     * @param node The Node to add.
     */
    public void add(Node node)
    {
        path.add(node);
    }


    /**
     * The size of the Path.
     * @return The size of the Path
     */
    public int size()
    {
        return path.size();
    }

    /**
     * Return the last node in the path or null if the path is empty.
     * @return the last node in the path or null if the path is empty.
     */
    public Node last()
    {
        if (size() == 0)
        {
            return null;
        }
        else
        {
            return (Node) path.get(size() - 1);
        }
    }


    /**
     * Returns the text that corresponds to applying the patches
     * in the list of nodes in the Path.
     * Assume that the text of the first node is plaintext and not
     * deltatext.
     * @return The resulting text after the patches
     */
    public List patch()
            throws InvalidFileFormatException,
            PatchFailedException,
            NodeNotFoundException
    {
        return patch(false);
    }

    /**
     * Returns the text that corresponds to applying the patches
     * in the list of nodes in the Path.
     * Assume that the text of the first node is plaintext and not
     * deltatext.
     * @param annotate if true, then each text line is a
     * {@link Line Line} with the original text annotated with
     * the revision in which it was last changed or added.
     * @return The resulting text after the patches
     */
    public List patch(boolean annotate)
            throws InvalidFileFormatException,
            PatchFailedException,
            NodeNotFoundException
    {
        return patch(new Lines(), annotate);
    }

    /**
     * Returns the text that corresponds to applying the patches
     * in the list of nodes in the Path.
     * Assume that the text of the first node is plaintext and not
     * deltatext.
     * @param lines The list to where the text must be added and the
     * patches applied.
     * {@link Line Line} with the original text annotated with
     * the revision in which it was last changed or added.
     * @return The resulting text after the patches
     */
    public List patch(List lines)
            throws InvalidFileFormatException,
            PatchFailedException,
            NodeNotFoundException
    {
        return patch(lines, false);
    }

    /**
     * Returns the text that corresponds to applying the patches
     * in the list of nodes in the Path.
     * Assume that the text of the first node is plaintext and not
     * deltatext.
     * @param lines The list to where the text must be added and the
     * patches applied.
     * @param annotate if true, then each text line is a
     * {@link Line Line} with the original text annotated with
     * the revision in which it was last changed or added.
     * @return The resulting text after the patches
     */
    public List patch(List lines, boolean annotate)
            throws InvalidFileFormatException,
            PatchFailedException,
            NodeNotFoundException
    {
        Iterator p = path.iterator();

        // get full text of first node
        TrunkNode head = (TrunkNode) p.next();
        head.patch0(lines, annotate);

        // the rest are patches
        while (p.hasNext())
        {
            Node n = (Node) p.next();
            n.patch(lines, annotate);
        }
        return lines;
    }
}
