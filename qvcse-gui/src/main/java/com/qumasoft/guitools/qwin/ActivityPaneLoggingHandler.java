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

package com.qumasoft.guitools.qwin;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Activity pane logging handler. A logging handler to populate the activity pane with log messages.
 *
 * @author Jim Voris
 */
public class ActivityPaneLoggingHandler extends java.util.logging.Handler {

    /**
     * Creates a new instance of ActivityPaneLoggingHandler.
     */
    public ActivityPaneLoggingHandler() {
        super();
        setFormatter(new ActivityPaneFormatter());
        setLevel(Level.ALL);
        setFilter(ActivityPaneLogFilter.getInstance());
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
        if (getFilter() != null) {
            if (!getFilter().isLoggable(record)) {
                return;
            }
        }
        String formattedLogRecord = getFormatter().format(record);
        ActivityListModel activityListModel = (ActivityListModel) QWinFrame.getQWinFrame().getActivityPane().getActivityList().getModel();
        activityListModel.addMessage(formattedLogRecord);
    }

    static class ActivityPaneFormatter extends java.util.logging.Formatter {

        ActivityPaneFormatter() {
            super();
        }

        @Override
        public String format(LogRecord record) {
            StringBuilder stringBuffer = new StringBuilder();
            Date timeStamp = new Date(record.getMillis());
            stringBuffer.append(record.getLevel().toString()).append(" ").append(timeStamp.toString()).append(" ");

            // Only provide all the info on WARNING messages.
            if (record.getLevel() == Level.WARNING) {
                if (record.getSourceClassName() != null) {
                    stringBuffer.append(record.getSourceClassName()).append(" ");
                }
                if (record.getSourceMethodName() != null) {
                    stringBuffer.append(record.getSourceMethodName()).append(" ");
                }
            }
            stringBuffer.append(record.getMessage());
            return stringBuffer.toString();
        }
    }

}
