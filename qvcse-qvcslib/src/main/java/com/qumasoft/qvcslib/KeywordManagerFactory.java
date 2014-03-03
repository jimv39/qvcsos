//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.qvcslib;

/**
 * Keyword manager factory. Builds the kind of keyword manager that we need. The original intent was to allow support for other keyword managers, but at this writing, the only
 * keyword manager that we support is the QVCS keyword manager.
 * @author Jim Voris
 */
public final class KeywordManagerFactory {

    private static final KeywordManagerFactory FACTORY = new KeywordManagerFactory();

    /**
     * Creates a new instance of KeywordManagerFactory.
     */
    private KeywordManagerFactory() {
    }

    /**
     * Get the singleton instance of the keyword manager factory.
     * @return the singleton instance of the keyword manager factory.
     */
    public static KeywordManagerFactory getInstance() {
        return FACTORY;
    }

    /**
     * Get the appropriate singleton keyword manager.
     * @return the appropriate keyword manager.
     */
    public KeywordManagerInterface getKeywordManager() {
        // Not much here right now... since the only keyword manager we
        // support is the native QVCS keyword manager.
        return QVCSKeywordManager.getInstance();
    }

    /**
     * Get a new (non-singleton) keyword manager.
     * @return a new (non-singleton) keyword manager.
     */
    public KeywordManagerInterface getNewKeywordManager() {
        // Not much here right now... since the only keyword manager we
        // support is the native QVCS keyword manager.
        return QVCSKeywordManager.getNewInstance();
    }
}
