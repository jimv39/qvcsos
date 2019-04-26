/*   Copyright 2004-2019 Jim Voris
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

import java.util.Date;
import java.util.Enumeration;

/**
 * A useful properties style base class.
 * @author Jim Voris
 */
public abstract class QumaProperties {

    private java.util.Properties actualProperties;
    private String propertyFileName;
    private final Object propertySyncObject = new Object();

    /**
     * Creates new QumaProperties.
     */
    public QumaProperties() {
    }

    /**
     * Set a string value.
     * @param tag the property tag for the string.
     * @param value the string value.
     */
    protected synchronized void setStringValue(String tag, String value) {
        if (actualProperties != null) {
            actualProperties.put(tag, value);
        }
    }

    /**
     * Get a string value.
     * @param tag the property tag for the string.
     * @return the string value.
     */
    protected synchronized String getStringValue(String tag) {
        String value = "";
        if (actualProperties != null) {
            value = actualProperties.getProperty(tag, "");
        }
        return value;
    }

    /**
     * Set a Boolean value.
     * @param tag the property tag for the Boolean.
     * @param flag the Boolean value.
     */
    protected synchronized void setBooleanValue(String tag, boolean flag) {
        if (actualProperties != null) {
            if (flag) {
                actualProperties.put(tag, QVCSConstants.QVCS_YES);
            } else {
                actualProperties.put(tag, QVCSConstants.QVCS_NO);
            }
        }
    }

    /**
     * Get a boolean value.
     * @param tag the property tag for the boolean.
     * @return the boolean value.
     */
    protected synchronized boolean getBooleanValue(String tag) {
        boolean returnFlag = false;
        if (actualProperties != null) {
            String stringFlag = actualProperties.getProperty(tag, QVCSConstants.QVCS_NO);
            if (stringFlag.compareToIgnoreCase(QVCSConstants.QVCS_YES) == 0) {
                returnFlag = true;
            }
        }
        return returnFlag;
    }

    /**
     * Set an integer value.
     * @param tag the property tag for the integer.
     * @param value the integer value.
     */
    protected synchronized void setIntegerValue(String tag, int value) {
        if (actualProperties != null) {
            actualProperties.put(tag, Integer.toString(value));
        }
    }

    /**
     * Get an integer value.
     * @param tag the property tag for the integer.
     * @return the integer value.
     */
    protected synchronized int getIntegerValue(String tag) {
        int returnInt = 0;
        if (actualProperties != null) {
            returnInt = Integer.decode(actualProperties.getProperty(tag, "0"));
        }
        return returnInt;
    }

    /**
     * Get an integer value. Return the supplied default value if the integer value property is not found.
     * @param tag the property tag for the integer.
     * @param defaultValue the integer's default value.
     * @return the integer value.
     */
    protected synchronized int getIntegerValue(String tag, int defaultValue) {
        int returnInt = defaultValue;
        String defaultReturnString = Integer.toString(defaultValue);
        if (actualProperties != null) {
            returnInt = Integer.decode(actualProperties.getProperty(tag, defaultReturnString));
        }
        return returnInt;
    }

    /**
     * Get a Date value.
     * @param tag the property tag for the Date.
     * @return the Date value. If the date property does not exist, then return a Date of now.
     */
    protected synchronized Date getDateValue(String tag) {
        Date date = new Date(0L);
        if (actualProperties != null) {
            long dateTime = Long.decode(actualProperties.getProperty(tag, "0"));
            date.setTime(dateTime);
        }
        return date;
    }

    /**
     * Set a Date value.
     * @param tag the property tag for the Date.
     * @param date the Date value.
     */
    protected synchronized void setDateValue(String tag, Date date) {
        if (actualProperties != null) {
            long dateTime = date.getTime();
            actualProperties.put(tag, Long.toString(dateTime));
        }
    }

    /**
     * A useful String representation of this object instance.
     * @return A useful String representation of this object instance.
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (actualProperties != null) {
            synchronized (propertySyncObject) {
                Enumeration<Object> keys = actualProperties.keys();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    String value = actualProperties.getProperty(key);
                    buffer.append(key).append(": ").append(value).append("\n");
                }
            }
        }
        return buffer.toString();
    }

    /**
     * Get the actual properties.
     * @return the actual properties.
     */
    public java.util.Properties getActualProperties() {
        return actualProperties;
    }

    /**
     * Set the actual properties.
     * @param properties the actual properties.
     */
    public final void setActualProperties(java.util.Properties properties) {
        this.actualProperties = properties;
    }

    /**
     * Get the property file name.
     * @return the property file name.
     */
    public final String getPropertyFileName() {
        return propertyFileName;
    }

    /**
     * Set the property file name.
     * @param fileName the property file name.
     */
    public final void setPropertyFileName(String fileName) {
        this.propertyFileName = fileName;
    }

    /**
     * Get the Sync object.
     * @return the Sync object.
     */
    public Object getPropertySyncObject() {
        return propertySyncObject;
    }
}
