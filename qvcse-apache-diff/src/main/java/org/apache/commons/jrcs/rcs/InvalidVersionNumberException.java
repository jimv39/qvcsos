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
 * Thrown whenever a given version number is invalid for the context.
 * This class is NOT thread safe.
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @version $Id: InvalidVersionNumberException.java,v 1.4 2004/02/28 03:35:36 bayard Exp $
 */

public class InvalidVersionNumberException
        extends IllegalArgumentException
{
    public InvalidVersionNumberException()
    {
    }

    public InvalidVersionNumberException(String v)
    {
        super(v);
    }

    public InvalidVersionNumberException(Version v)
    {
        super(v.toString());
    }
}
