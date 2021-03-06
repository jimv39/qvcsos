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
 * Thrown whenever the differencing engine cannot produce the differences
 * between two revisions of ta text.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/28 03:35:36 $
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @see Diff
 * @see DiffAlgorithm
 */
public class DifferentiationFailedException extends DiffException
{

    public DifferentiationFailedException()
    {
    }

    public DifferentiationFailedException(String msg)
    {
        super(msg);
    }
}

