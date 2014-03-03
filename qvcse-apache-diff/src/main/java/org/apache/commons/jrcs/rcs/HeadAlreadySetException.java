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
 * Thrown if the ArchiveParser finds that the head node is set more than once.
 * This class is NOT thread safe.
 *
 * @see Archive
 * @see ArchiveParser
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @version $Id: HeadAlreadySetException.java,v 1.4 2004/02/28 03:35:36 bayard Exp $
 */
public class HeadAlreadySetException
        extends IllegalArgumentException
{
    public HeadAlreadySetException()
    {
    }

    public HeadAlreadySetException(String v)
    {
        super(v);
    }

    public HeadAlreadySetException(Version v)
    {
        super(v.toString());
    }
}
