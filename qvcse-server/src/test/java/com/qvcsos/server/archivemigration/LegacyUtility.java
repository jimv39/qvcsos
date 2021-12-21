/*
 * Copyright 2021 Jim Voris.
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
package com.qvcsos.server.archivemigration;

/**
 *
 * @author Jim Voris
 */
class LegacyUtility {
    private static final int EXTENSION_LENGTH_WITH_PERIOD = 4;
    private static final String EMPTY_EXTENSION_EXTENSION = ".___";

    static String convertArchiveNameToShortWorkfileName(String archiveName) {
        String nameSeparator = ".";
        byte pathSeparator = LegacyQVCSConstants.QVCS_STANDARD_PATH_SEPARATOR;

        // Search the archiveName backwards, looking for the path separator.
        int pathSeparatorIndex = archiveName.lastIndexOf(pathSeparator);

        // Make a StringBuilder we'll use for changing the name
        StringBuilder tempWorkfileName = new StringBuilder(archiveName);

        // Search the archiveName backwards, looking for the name separator.
        int nameSeparatorIndex = archiveName.lastIndexOf(nameSeparator);
        if (nameSeparatorIndex != -1) {
            String extension = archiveName.substring(nameSeparatorIndex);
            if (0 == extension.compareTo(EMPTY_EXTENSION_EXTENSION)) {
                // Strip off the fake extension to create the workfile name.
                tempWorkfileName.setLength(archiveName.length() - EXTENSION_LENGTH_WITH_PERIOD);
            } else if (0 == extension.compareTo(".___qvcsArchive")) {
                // the workfile name has an extension of '___'.
                tempWorkfileName.setLength(archiveName.length() - ".___qvcsArchive".length());
                tempWorkfileName.append(EMPTY_EXTENSION_EXTENSION);
            } else {
                int lastIndex = archiveName.length() - 1;
                int extensionIndex = nameSeparatorIndex + 1;
                for (; extensionIndex <= lastIndex; extensionIndex++) {
                    switch (archiveName.charAt(extensionIndex)) {
                        case ',':
                        case '?':
                        case '*':
                        case '_':
                        case '^':
                        case '~':
                        case '!':
                        case '-':
                        case '{':
                        case '}':
                        case '(':
                        case ')':
                        case '+':
                        case '\'':
                            break;

                        case 'a':
                            tempWorkfileName.setCharAt(extensionIndex, 'z');
                            break;

                        case 'A':
                            tempWorkfileName.setCharAt(extensionIndex, 'Z');
                            break;

                        case '0':
                            tempWorkfileName.setCharAt(extensionIndex, '9');
                            break;

                        default:
                            tempWorkfileName.setCharAt(extensionIndex, (char) (archiveName.charAt(extensionIndex) - 1));
                            break;
                    }
                }
            }
        }
        String returnString = new String(tempWorkfileName);
        if (pathSeparatorIndex != -1) {
            returnString = returnString.substring(pathSeparatorIndex + 1);
        }

        return returnString;
    }

    static String convertWorkfileNameToShortArchiveName(String workfileName) {
        byte pathSeparator = '/';
        String nameSeparator = ".";

        // Search the workfileName backwards, looking for the path separator.
        int pathSeparatorIndex = workfileName.lastIndexOf(pathSeparator);

        // Make a StringBuilder we'll use for changing the name
        StringBuilder tempArchiveName = new StringBuilder(workfileName);

        // Search the archiveName backwards, looking for the name separator.
        int nameSeparatorIndex = workfileName.lastIndexOf(nameSeparator);
        if (nameSeparatorIndex != -1) {
            String extension = workfileName.substring(nameSeparatorIndex);
            if (extension.length() == 0) {
                // Append the EMPTY_EXTENSION_EXTENSION to the workfile name to create the archive name.
                tempArchiveName.append(EMPTY_EXTENSION_EXTENSION);
            } else if (0 == extension.compareTo(EMPTY_EXTENSION_EXTENSION)) {
                tempArchiveName.setLength(workfileName.length() - EXTENSION_LENGTH_WITH_PERIOD);
                tempArchiveName.append(".___qvcsArchive");
            } else {
                int lastIndex = workfileName.length() - 1;
                int extensionIndex = nameSeparatorIndex + 1;
                for (; extensionIndex <= lastIndex; extensionIndex++) {
                    switch (workfileName.charAt(extensionIndex)) {
                        case ',':
                        case '?':
                        case '*':
                        case '_':
                        case '^':
                        case '~':
                        case '!':
                        case '-':
                        case '{':
                        case '}':
                        case '(':
                        case ')':
                        case '+':
                        case '\'':
                            break;

                        case 'z':
                            tempArchiveName.setCharAt(extensionIndex, 'a');
                            break;

                        case 'Z':
                            tempArchiveName.setCharAt(extensionIndex, 'A');
                            break;

                        case '9':
                            tempArchiveName.setCharAt(extensionIndex, '0');
                            break;

                        default:
                            tempArchiveName.setCharAt(extensionIndex, (char) (workfileName.charAt(extensionIndex) + 1));
                            break;
                    }
                }
            }
        } else {
            // Append the EMPTY_EXTENSION_EXTENSION to the workfile name to create the archive name.
            tempArchiveName.append(EMPTY_EXTENSION_EXTENSION);
        }

        String returnString = new String(tempArchiveName);
        if (pathSeparatorIndex != -1) {
            returnString = returnString.substring(pathSeparatorIndex + 1);
        }

        return returnString;
    }

}
