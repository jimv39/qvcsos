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
 * A simple interface for implementations of differencing algorithms.
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/28 03:35:36 $
 *
 * @author <a href="mailto:bwm@hplb.hpl.hp.com">Brian McBride</a>
 */
public interface DiffAlgorithm {

    /**
     * Computes the difference between the original sequence and the revised sequence and returns it as a {@link org.apache.commons.jrcs.diff.Revision Revision} object. <p> The revision can be used to
     * construct the revised sequence from the original sequence.
     *
     * @param rev the revised text
     * @return the revision script.
     * @throws DifferentiationFailedException if the diff could not be computed.
     */
    Revision diff(Object[] orig, Object[] rev) throws DifferentiationFailedException;
}
