/*   Copyright 2004-2014 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.guitools.qwin.filefilter;

import java.util.Objects;

/**
 * Abstract file filter.
 * @author Jim Voris
 */
public abstract class AbstractFileFilter implements FileFilterInterface {
    private static final long serialVersionUID = 264146458472594577L;

    private final boolean isANDFilterFlag;

    /**
     * Creates a new instance of AbstractFileFilter.
     * @param flag is this an AND (true) type of filter.
     */
    public AbstractFileFilter(boolean flag) {
        isANDFilterFlag = flag;
    }

    @Override
    public boolean getIsANDFilter() {
        return isANDFilterFlag;
    }

    @Override
    public boolean getIsORFilter() {
        return !isANDFilterFlag;
    }

    @Override
    public boolean requiresRevisionDetailInfo() {
        return false;
    }

    /**
     * Helper method to compute a useful hash value for file filter classes.
     * @param value the raw filter data.
     * @param isANDFlag is this an AND filter.
     * @return the hash to use for the given value/isANDFlag.
     */
    public int computeHash(Object value, boolean isANDFlag) {
        // <editor-fold>
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(value);
        if (this.getIsANDFilter()) {
            hash += 97;
        }
        // </editor-fold>
        return hash;
    }

    @Override
    public abstract String getFilterData();
}
