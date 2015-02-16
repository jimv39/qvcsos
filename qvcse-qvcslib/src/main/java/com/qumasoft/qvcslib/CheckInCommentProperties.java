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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store check in comments in a property file. This is used as a simple way to allow the user to cycle through the most recent
 * check in comments.
 * @author Jim Voris
 */
public final class CheckInCommentProperties extends QumaProperties {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckInCommentProperties.class);
    private static final String MAXIMUM_COMMENT_COUNT = "MaxCommentCount";
    private static final int MAXIMUM_COMMENT_COUNT_VALUE  = 10;
    private static final String COMMENT_COUNT = "CommentCount";
    private static final String CHECKIN_COMMENT = "CheckInComment";
    private List<String> commentArray;
    private int maximumCommentCount;

    /**
     * Creates a new instance of CheckInCommentProperties.
     * @param userName the QVCS user name.
     */
    public CheckInCommentProperties(String userName) {
        setPropertyFileName(System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_USER_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_CHECKIN_COMMENTS_PREFIX
                + userName
                + ".properties");

        try {
            commentArray = new ArrayList<>(MAXIMUM_COMMENT_COUNT_VALUE);
            loadProperties(getPropertyFileName());
        } catch (QVCSException e) {
            // Catch any exception.  If the property file is missing, we'll just go
            // with the defaults.
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    private synchronized void loadProperties(String propertyFilename) throws QVCSException {
        FileInputStream inStream = null;
        java.util.Properties defaultProperties = new java.util.Properties();

        // Define some default values
        defaultProperties.put(MAXIMUM_COMMENT_COUNT, "10");
        defaultProperties.put(COMMENT_COUNT, "0");

        setActualProperties(new java.util.Properties(defaultProperties));
        try {
            inStream = new FileInputStream(new File(propertyFilename));
            getActualProperties().load(inStream);
            populateCommentArray();
            maximumCommentCount = getMaximumCommentCount();
        } catch (IOException e) {
            LOGGER.warn("Checkin comments file not found: [{}]", propertyFilename);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing check-in comments properties file: " + propertyFilename + ". Exception: " + e.getClass().toString() + ": "
                            + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Get the maximum number of comments.
     * @return the maximum number of comments.
     */
    public synchronized int getMaximumCommentCount() {
        return getIntegerValue(MAXIMUM_COMMENT_COUNT, MAXIMUM_COMMENT_COUNT_VALUE);
    }

    /**
     * Set the maximum number of comments.
     * @param maxCommentCount the maximum number of comments.
     */
    public synchronized void setMaximumCommentCount(int maxCommentCount) {
        setIntegerValue(MAXIMUM_COMMENT_COUNT, maxCommentCount);
        this.maximumCommentCount = maxCommentCount;
    }

    /**
     * Get the number of comments.
     * @return the number of comments.
     */
    public synchronized int getCommentCount() {
        return getIntegerValue(COMMENT_COUNT, 0);
    }

    private synchronized String localGetCheckInComment(int index) {
        String tag = getCommentTag(index);
        return getStringValue(tag);
    }

    /**
     * Get the given checkin comment.
     * @param index the index of the comment we are interested in.
     * @return the checking comment for the given index.
     */
    public synchronized String getCheckInComment(int index) {
        String checkinComment = null;
        if (index < commentArray.size()) {
            checkinComment = commentArray.get(index);
        }
        return checkinComment;
    }

    /**
     * Add a checkin comment.
     * @param checkInComment a checkin comment.
     */
    public synchronized void addCheckInComment(String checkInComment) {
        List<String> newCommentArray;

        // See if the comment is already present.
        for (int i = 0; i < commentArray.size(); i++) {
            String existingComment = commentArray.get(i);
            if (checkInComment.equals(existingComment)) {
                // If the comment is already at the beginning, we don't need
                // to do anything.
                if (i == 0) {
                    return;
                }

                // We need a new container...
                newCommentArray = new ArrayList<>(commentArray.size() + 1);

                // Move this comment to the front.
                newCommentArray.add(0, existingComment);

                for (int j = 0; j < commentArray.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    newCommentArray.add(commentArray.get(j));
                }
                commentArray = newCommentArray;
                return;
            }
        }

        // We have a new comment, so we need a new container...
        newCommentArray = new ArrayList<>(commentArray.size() + 1);

        // Comment does not exist yet.  Put it first.
        newCommentArray.add(0, checkInComment);

        // Copy the remaining existing comments to the new container.
        int commentCount;
        if (commentArray.size() < (maximumCommentCount - 1)) {
            commentCount = commentArray.size();
        } else {
            commentCount = maximumCommentCount - 1;
        }
        for (int i = 0; i < commentCount; i++) {
            newCommentArray.add(commentArray.get(i));
        }

        commentArray = newCommentArray;
    }

    /**
     * Write the comments to disk.
     */
    public synchronized void saveProperties() {
        FileOutputStream outStream = null;
        if (getActualProperties() != null) {
            try {
                // Make sure the maximum comment count is in the property container.
                setIntegerValue(MAXIMUM_COMMENT_COUNT, maximumCommentCount);

                // Limit the comment count to what the user says the max should be.
                int commentCount;
                if (commentArray.size() > getMaximumCommentCount()) {
                    commentCount = getMaximumCommentCount();
                } else {
                    commentCount = commentArray.size();
                }

                // Put the number of comments into the property container.
                setIntegerValue(COMMENT_COUNT, commentCount);

                // Put the comment strings into the property container.
                for (int i = 0; i < commentCount; i++) {
                    setStringValue(getCommentTag(i), commentArray.get(i));
                }

                outStream = new FileOutputStream(new File(getPropertyFileName()));

                // Write the properties to the property file.
                getActualProperties().store(outStream, "QVCS CheckIn Comments for user: " + System.getProperty("user.name"));
            } catch (IOException e) {
                // Catch any exception.  If the property file is missing, we'll just go
                // with the defaults.
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Exception in closing check-in comments properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString()
                                + ": " + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    private synchronized void populateCommentArray() {
        commentArray = new ArrayList<>();
        int commentCount = getCommentCount();
        for (int i = 0; i < commentCount; i++) {
            commentArray.add(i, localGetCheckInComment(i));
        }
    }

    private String getCommentTag(int index) {
        return CHECKIN_COMMENT + Integer.toString(index);
    }
}
