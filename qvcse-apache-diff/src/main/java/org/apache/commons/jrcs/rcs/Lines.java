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

import java.text.Format;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.jrcs.diff.Diff;

/**
 * A list of the lines in the text of a revision annotated with the
 * version that corresponds to each line.
 *
 * @see Line
 * @see Archive
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @version $Id: Lines.java,v 1.4 2004/02/28 03:35:36 bayard Exp $
 */
class Lines
        extends ArrayList
{

    public static final Format annotationFormat = new MessageFormat(
            "{0,,        } ({1} {2,  date,dd-MMM-yyyy}):"
    );

    public Lines()
    {
    }

    public Lines(String text)
    {
        this(null, Diff.stringToArray(text));
    }

    public Lines(Node release, String text)
    {
        this(release, Diff.stringToArray(text));
    }

    public Lines(Object[] text)
    {
        this(null, text);
    }

    public Lines(Node release, Object[] text)
    {
        for (int i = 0; i < text.length; i++)
        {
            super.add(new Line(release, text[i]));
        }
    }

    public boolean add(Object o)
    {
        return super.add((Line) o);
    }

    public Object[] toArray()
    {
        return toArray(false);
    }

    public Object[] toArray(boolean annotate)
    {
        Object[] result = new Object[this.size()];
        Iterator r = this.iterator();
        int i = 0;
        while (r.hasNext())
        {
            Line l = (Line) r.next();
            Object o = l.getText();
            if (annotate)
            {
                Node rev = l.getRevision();
                o = annotationFormat.format(new Object[]{rev.getVersion(), rev.getAuthor(), rev.getDate()});
            }
            result[i++] = o;
        }
        return result;
    }

    public String toString()
    {
        return toString(false);
    }

    public String toString(boolean annotate)
    {
        return Diff.arrayToString(this.toArray(annotate));
    }
}



