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
package com.qumasoft.qvcslib;

/**
 * Client expansion context. Helper class to encapsulate client supplied parameters needed to expand keywords. Instances are immutable.
 * @author Jim Voris
 */
public class ClientExpansionContext {
    private final String serverName;
    private final UserProperties userProperties;
    private final UserLocationProperties userLocationProperties;
    private final int revisionIndex;
    private final String labelString;
    private final boolean expandKeywordsFlag;

    /**
     * Create a client expansion context instance using the given parameters.
     * @param server the server name.
     * @param usrProperties the user properties.
     * @param usrLocationProperties the user location properties.
     * @param revIndex the revision index.
     * @param label the label string.
     * @param xpandKeywordsFlag expand keywords flag.
     */
    public ClientExpansionContext(String server, UserProperties usrProperties, UserLocationProperties usrLocationProperties, int revIndex,
                                  String label, boolean xpandKeywordsFlag) {
        this.serverName = server;
        this.userProperties = usrProperties;
        this.userLocationProperties = usrLocationProperties;
        this.revisionIndex = revIndex;
        this.labelString = label;
        this.expandKeywordsFlag = xpandKeywordsFlag;
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Get the user properties.
     * @return the user properties.
     */
    public UserProperties getUserProperties() {
        return userProperties;
    }

    /**
     * Get the user location properties.
     * @return the user location properties.
     */
    public UserLocationProperties getUserLocationProperties() {
        return userLocationProperties;
    }

    /**
     * Get the revision index.
     * @return the revision index.
     */
    public int getRevisionIndex() {
        return revisionIndex;
    }

    /**
     * Get the label string.
     * @return the label string.
     */
    public String getLabelString() {
        return labelString;
    }

    /**
     * Get the expand keywords flag.
     * @return the expand keywords flag.
     */
    public boolean getExpandKeywordsFlag() {
        return expandKeywordsFlag;
    }
}
