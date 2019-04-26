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
package com.qumasoft.guitools.qwin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
/**
 * Activity pane log filter. This is a singleton.
 *
 * @author Jim Voris
 */
public final class ActivityPaneLogFilter {

    private final Logger root;
    private Level logLevel;
    private static final ActivityPaneLogFilter ACTIVITY_PANE_LOG_FILTER = new ActivityPaneLogFilter();

    /**
     * Creates a new instance of ActivityPaneLogFilter.
     */
    private ActivityPaneLogFilter() {
        root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        this.logLevel = root.getLevel();
    }

    /**
     * Get the Activity pane log filter singleton.
     *
     * @return the Activity pane log filter singleton.
     */
    public static ActivityPaneLogFilter getInstance() {
        return ACTIVITY_PANE_LOG_FILTER;
    }

    /**
     * Set the log level for the activity pane.
     *
     * @param newLevel the new log level.
     */
    public void setLevel(Level newLevel) {
        logLevel = newLevel;
        root.setLevel(newLevel);
    }

    /**
     * Get the log level.
     *
     * @return the log level.
     */
    public Level getLevel() {
        return logLevel;
    }

}
