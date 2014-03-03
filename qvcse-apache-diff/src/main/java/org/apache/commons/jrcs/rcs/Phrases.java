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
import java.util.TreeMap;

/**
 * A set of "new phrases" for an Archive. Phrases are keyed lists of symbols. An Archive stores the keys it doesn't recognizes in a Phrases set to preserve them. Unrecognized keys probably belong to
 * archive extensions.
 *
 * @see Archive
 *
 * @author <a href="mailto:juanco@suigeneris.org">Juanco Anez</a>
 * @version $Id: Phrases.java,v 1.5 2004/02/28 03:35:36 bayard Exp $
 */
class Phrases extends TreeMap {
    private static final long serialVersionUID = 1L;

    public void toString(StringBuffer s, String EOL) {
        Iterator i = keySet().iterator();
        while (i.hasNext()) {
            String key = i.next().toString();
            String value = get(key).toString();
            s.append(key);
            s.append(" ");
            s.append(value);
            s.append(EOL);
        }
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();
        toString(s, Archive.RCS_NEWLINE);
        return s.toString();
    }
}
