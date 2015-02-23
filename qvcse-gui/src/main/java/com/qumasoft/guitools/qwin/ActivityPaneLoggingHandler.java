//   Copyright 2004-2015 Jim Voris
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

import ch.qos.logback.classic.spi.LoggingEvent;
import java.util.Date;

/**
 * Activity pane logging handler. A logging handler to populate the activity pane with log messages.
 *
 * @author Jim Voris
 */
public class ActivityPaneLoggingHandler extends ch.qos.logback.core.AppenderBase {

    /**
     * Creates a new instance of ActivityPaneLoggingHandler.
     */
    public ActivityPaneLoggingHandler() {
        super();
    }

    @Override
    protected void append(Object eventObject) {
        if (!isStarted()) {
            return;
        }
        if (eventObject instanceof LoggingEvent) {
            LoggingEvent event = (LoggingEvent) eventObject;
            String formattedLogRecord = event.getFormattedMessage();
            StringBuilder stringBuffer = new StringBuilder();
            Date timeStamp = new Date(event.getTimeStamp());
            stringBuffer.append(event.getLevel().toString()).append(" ").append(timeStamp.toString()).append(" ").append(formattedLogRecord);
            ActivityListModel activityListModel = (ActivityListModel) QWinFrame.getQWinFrame().getActivityPane().getActivityList().getModel();
            activityListModel.addMessage(stringBuffer.toString());
        }
    }
}
