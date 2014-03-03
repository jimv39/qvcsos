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
 * Thrown if the version number given for a trunk node is invalid.
 * Version numbers for trunk nodes must be of the form x.y .
 * This class is NOT thread safe.
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @version $Id: InvalidTrunkVersionNumberException.java,v 1.4 2004/02/28 03:35:36 bayard Exp $
 */
public class InvalidTrunkVersionNumberException
        extends InvalidVersionNumberException
{

    public InvalidTrunkVersionNumberException()
    {
    }

    public InvalidTrunkVersionNumberException(String v)
    {
        super(v);
    }

    public InvalidTrunkVersionNumberException(Version v)
    {
        super(v);
    }
}
