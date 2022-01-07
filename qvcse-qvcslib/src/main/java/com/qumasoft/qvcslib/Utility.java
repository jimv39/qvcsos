/*
 * Copyright 2014-2021 JimVoris.
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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton utility class for methods that are global in nature.
 *
 * @author Jim Voris
 */
public final class Utility {
    // Create our logger object

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Utility.class);
    private static final int EXTENSION_LENGTH_WITH_PERIOD = 4;
    private static final Utility UTILITY = new Utility();
    private MessageDigest messageDigest = null;
    private static final String EMPTY_EXTENSION_EXTENSION = ".___";

    /**
     * Possible values for overwrite behavior.
     */
    public enum OverwriteBehavior {

        /**
         * Ask before overwriting a writable file.
         */
        ASK_BEFORE_OVERWRITE_OF_WRITABLE_FILE,
        /**
         * Do not replace a writable file.
         */
        DO_NOT_REPLACE_WRITABLE_FILE,
        /**
         * Replace a writable file.
         */
        REPLACE_WRITABLE_FILE
    }

    /**
     * Possible values for timestamp behavior for applying the timestamp to a fetched workfile.
     */
    public enum TimestampBehavior {

        /**
         * Set the workfile timestamp to now.
         */
        SET_TIMESTAMP_TO_NOW,
        /**
         * Set the workfile timestamp to when the fetched revision was checked in.
         */
        SET_TIMESTAMP_TO_CHECKIN_TIME,
        /**
         * Set the workfile timestamp to the last edit time of the fetched revision.
         */
        SET_TIMESTAMP_TO_EDIT_TIME
    }

    /**
     * Creates a new instance of Utility.
     */
    private Utility() {
        try {
            messageDigest = MessageDigest.getInstance(QVCSConstants.QVCSOS_DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            messageDigest = null;
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Get the Utility singleton instance.
     *
     * @return the Utility singleton instance.
     */
    public static Utility getInstance() {
        return UTILITY;
    }

    /**
     * Return the digest for given password.
     *
     * @param password the password to hash.
     * @return the digest of the password.
     */
    public synchronized byte[] hashPassword(String password) {
        byte[] hashedPassword = null;

        if (messageDigest != null) {
            messageDigest.reset();
            hashedPassword = messageDigest.digest(password.getBytes());
        }
        return hashedPassword;
    }

    /**
     * Figure out what character is used as the path separator in the given appendedPath.
     *
     * @param appendedPath the appendedPath String to examine.
     * @return the path separator character.
     */
    public static byte deducePathSeparator(String appendedPath) {
        byte pathSeparator;

        // Search for a path separator.... it might be '\', or it might be '/'
        int pathSeparatorCandidateForwardSlashIndex = appendedPath.lastIndexOf('/');
        int pathSeparatorCandidateBackSlashIndex = appendedPath.lastIndexOf('\\');

        if (pathSeparatorCandidateForwardSlashIndex > pathSeparatorCandidateBackSlashIndex) {
            pathSeparator = '/';
        } else if (pathSeparatorCandidateBackSlashIndex > -1) {
            pathSeparator = '\\';
        } else {
            pathSeparator = QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR;
        }
        return pathSeparator;
    }

    /**
     * Convert the given appendedPath to one that uses the QVCS standard path separator: the '/' character.
     *
     * @param appendedPath the appendedPath to examine.
     * @return an appendedPath String that uses the QVCS standard path separator.
     */
    public static String convertToStandardPath(String appendedPath) {
        byte[] appendedPathBytes = appendedPath.getBytes();
        for (int i = 0; i < appendedPathBytes.length; i++) {
            if (appendedPathBytes[i] == '\\') {
                appendedPathBytes[i] = QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR;
            }
        }
        String returnedAppendedPath = new String(appendedPathBytes);

        return returnedAppendedPath;
    }

    /**
     * Get the directory segments of a given appendedPath. This method returns a String array of the directory segments that compose an appendedPath.
     *
     * @param appendedPath the appendedPath to examine. This method works for all appendedPaths, irrespective of what character is used for the path separator.
     * @return a String[] of the directory segments of the given appendedPath.
     */
    public static String[] getDirectorySegments(String appendedPath) {
        byte[] appendedPathBytes = appendedPath.getBytes();
        List<String> segments = new ArrayList<>();
        int startingIndex = 0;
        byte pathSeparator = deducePathSeparator(appendedPath);
        for (int i = 0; i < appendedPathBytes.length; i++) {
            if (appendedPathBytes[i] == pathSeparator) {
                String segment = new String(appendedPathBytes, startingIndex, i - startingIndex);
                segments.add(segment);
                startingIndex = i + 1;
            }
        }

        // Add the last segment
        String segment = new String(appendedPathBytes, startingIndex, appendedPathBytes.length - startingIndex);
        segments.add(segment);

        // Convert the list to a string[]
        String[] returnSegments = new String[segments.size()];
        Iterator it = segments.iterator();
        int i = 0;
        while (it.hasNext()) {
            String returnSegment = (String) it.next();
            returnSegments[i++] = returnSegment;
        }

        return returnSegments;
    }

    /**
     * Get the last segment of an appendedPath.
     *
     * @param appendedPath the appendedPath to examine.
     * @return the last segment of the given appendedPath.
     */
    public static String getLastDirectorySegment(final String appendedPath) {
        String[] directorySegments = getDirectorySegments(appendedPath);
        return directorySegments[directorySegments.length - 1];
    }

    /**
     * Convert an archive name to its corresponding short workfile name.
     *
     * @param archiveName the archive name.
     * @return the corresponding workfile name.
     */
    public static String convertArchiveNameToShortWorkfileName(String archiveName) {
        String nameSeparator = ".";
        byte pathSeparator = Utility.deducePathSeparator(archiveName);

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

    /**
     * Convert the workfile name to a short archive name.
     *
     * @param workfileName the workfile name to convert.
     * @return the associated archive file name.
     */
    public static String convertWorkfileNameToShortArchiveName(String workfileName) {
        byte pathSeparator = Utility.deducePathSeparator(workfileName);
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

    /**
     * Convert a workfile name to a short workfile name.
     *
     * @param workfileName the workfile name to convert.
     * @return the short workfile name.
     */
    public static String convertWorkfileNameToShortWorkfileName(String workfileName) {
        String retVal = workfileName;
        byte[] bytes = workfileName.getBytes();
        int index = -1;

        // We cannot use File.separator since the client may not use the
        // same separator as the server.
        for (int i = bytes.length - 1; i >= 0; i--) {
            if (bytes[i] == '/' || bytes[i] == '\\') {
                index = i;
                break;
            }
        }
        if (index != -1) {
            retVal = workfileName.substring(index + 1);
        }
        return retVal;
    }

    /**
     * Create a standard format for a file name for use in the activity journal.
     *
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param appendedPath the appended path.
     * @param shortWorkfileName the short workfile name.
     * @return a file name formatted for use in the activity journal.
     */
    public static String formatFilenameForActivityJournal(String projectName, String branchName, String appendedPath, String shortWorkfileName) {
        String appendedPathString;
        if (appendedPath.length() > 0) {
            appendedPathString = Utility.convertToStandardPath(appendedPath) + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING;
        } else {
            appendedPathString = "";
        }
        return projectName + "//" + branchName + "::" + appendedPathString + shortWorkfileName;
    }

    /**
     * Figure out the file extension for a filename. If the filename has no extension, then return the "" string;
     *
     * @param filename the file name to examine.
     * @return the file's file extension.
     */
    public static String getFileExtension(final String filename) {
        String extension;
        if (filename.lastIndexOf('.') != -1) {
            extension = filename.substring(1 + filename.lastIndexOf('.'));
        } else {
            extension = "";
        }
        return extension;
    }

    /**
     * Strip the file extension from a file name.
     *
     * @param filename the filename to examine.
     * @return the file name with its extension stripped off.
     */
    public static String stripFileExtension(final String filename) {
        String baseFilename;
        if (filename.lastIndexOf('.') != -1) {
            baseFilename = filename.substring(0, filename.lastIndexOf('.'));
        } else {
            baseFilename = filename;
        }
        return baseFilename;
    }

    public static File writeBufferToFile(byte[] inputBuffer) {
        FileOutputStream outStream = null;
        File tempFile;
        try {
            tempFile = File.createTempFile("qvcsostemp_", "tmp");
            tempFile.deleteOnExit();
        } catch (IOException e) {
            // There is no point in proceeding.  Report the problem, and bail.
            LOGGER.warn(e.getLocalizedMessage(), e);
            tempFile = null;
        }
        if (tempFile != null) {
            try {
                outStream = new java.io.FileOutputStream(tempFile);
                outStream.write(inputBuffer);
            } catch (IOException e) {
                tempFile = null;
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        tempFile = null;
                    }
                }
            }
        }
        return tempFile;
    }
    /**
     * Routine to display a URL in the default browser.
     *
     * @param url the url to display.
     */
    public static void openURL(String url) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Runtime.getRuntime().exec(new String[]{"open", url});
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                //assume Unix or Linux
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            String errMsg = "Error attempting to launch web browser";
            JOptionPane.showMessageDialog(null, errMsg + ":\n" + e.getLocalizedMessage());
        }
    }

    /**
     * Are we running on an Apple Mac computer.
     *
     * @return true if running on a Mac; false otherwise.
     */
    public static boolean isMacintosh() {
        boolean isMac = false;
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Mac")) {
            isMac = true;
        }
        return isMac;
    }

    /**
     * Are we running on some flavor of Linux.
     *
     * @return true if running on Linux; false otherwise.
     */
    public static boolean isLinux() {
        boolean isLinux = false;
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            isLinux = true;
        }
        return isLinux;
    }

    /**
     * Expand the stack trace for a given throwable into a String.
     *
     * @param throwable the throwable for which we'll create a String of its stack trace.
     * @return a String of the throwable's stack trace.
     */
    public static String expandStackTraceToString(Throwable throwable) {
        final Writer result = new StringWriter();
        if (throwable != null) {
            final PrintWriter printWriter = new PrintWriter(result);
            throwable.printStackTrace(printWriter);
        }
        return result.toString();
    }

    /**
     * Make sure a given path ends with a path separator character.
     *
     * @param path the prospective path.
     * @return the path guaranteed to end with a path separator character.
     */
    public static String endsWithPathSeparator(final String path) {
        String returnedPath;
        if (path.endsWith("/") || path.endsWith("\\")) {
            returnedPath = path;
        } else {
            returnedPath = path + File.separator;
        }
        return returnedPath;
    }

    /**
     * Make sure a given path does not end with a path separator character.
     *
     * @param path the prospective path.
     * @return the path guaranteed to NOT end with a path separator character.
     */
    public static String endsWithoutPathSeparator(final String path) {
        String returnedPath;
        if (path.endsWith("/") || path.endsWith("\\")) {
            returnedPath = path.substring(0, path.length() - 1);
        } else {
            returnedPath = path;
        }
        return returnedPath;
    }

    /**
     * Create an appendedPath String from the given directory segments.
     * @param segments a String[] of directory segments that will compose the appendedPath.
     * @return an appendedPath String for the given directory segments.
     */
    public static String createAppendedPathFromSegments(String[] segments) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            buffer.append(segments[i]);
            if (i < segments.length - 1) {
                buffer.append(File.separator);
            }
        }
        return buffer.toString();
    }

    /**
     * Deduce the original file name so we can recover a file from the cemetery. This information is extracted from the revision comment of the tip revision.
     * @param archiveInfo the archive information for a file that resides in the cemetery.
     * @return the file's original file name.
     */
    public static String deduceOriginalFilenameForUndeleteFromCemetery(ArchiveInfoInterface archiveInfo) {
        RevisionInformation revisionInformation = archiveInfo.getRevisionInformation();
        String deleteArchiveRevisionDescription = revisionInformation.getRevisionHeader(0).getRevisionDescription();
        String prefixString = "Deleted: '";
        int deletedStartingIndex = deleteArchiveRevisionDescription.indexOf(prefixString);
        String beginningOfOriginalFilename = deleteArchiveRevisionDescription.substring(deletedStartingIndex + prefixString.length());
        int endingIndex = beginningOfOriginalFilename.indexOf("' to cemetery");
        String originalFilename = beginningOfOriginalFilename.substring(0, endingIndex);
        originalFilename = Utility.convertToStandardPath(originalFilename);
        return originalFilename;
    }

    /**
     * Deduce the type of merge that must be performed.
     * @param infoForMerge information for the merge.
     * @param shortWorkfileName the short workfile name.
     * @return the type of merge that needs to be performed.
     * @throws QVCSException if we cannot figure out the kind of merge that should be performed.
     */
    public static PromotionType deduceTypeOfMerge(InfoForMerge infoForMerge, final String shortWorkfileName) throws QVCSException {
        PromotionType typeOfMerge = PromotionType.UNKNOWN_PROMOTION_TYPE;
        outerLoop:
        for (PromotionType mergeType : PromotionType.values()) {
            switch (mergeType) {
                case SIMPLE_PROMOTION_TYPE: // 000 -- no renames, moves, create, or delete.
                    if (!infoForMerge.getNameChangedFlag()
                            && !infoForMerge.getNameChangedFlag()
                            && !infoForMerge.getLocationChangedFlag()
                            && !infoForMerge.getCreatedOnBranchFlag()
                            && !infoForMerge.getDeletedOnBranchFlag()) {
                        // The file is in the same place as it was for the parent.
                        typeOfMerge = mergeType;
                        break outerLoop;
                    }
                    break;
                case FILE_CREATED_PROMOTION_TYPE:
                    if (infoForMerge.getCreatedOnBranchFlag()) {
                        typeOfMerge = mergeType;
                        break outerLoop;
                    }
                    break;
                case FILE_DELETED_PROMOTION_TYPE:
                    if (infoForMerge.getDeletedOnBranchFlag()) {
                        typeOfMerge = mergeType;
                        break outerLoop;
                    }
                    break;
                case FILE_NAME_CHANGE_PROMOTION_TYPE: // 001 -- name changed.
                    if (infoForMerge.getNameChangedFlag()
                            && !infoForMerge.getLocationChangedFlag()
                            && !infoForMerge.getCreatedOnBranchFlag()) {
                        typeOfMerge = mergeType;
                        break outerLoop;
                    }
                    break;
                case FILE_LOCATION_CHANGE_PROMOTION_TYPE: // 010 -- location changed.
                    if (!infoForMerge.getNameChangedFlag()
                            && infoForMerge.getLocationChangedFlag()
                            && !infoForMerge.getCreatedOnBranchFlag()) {
                        // The file names are the same, but they are in different directories.
                        typeOfMerge = mergeType;
                        break outerLoop;
                    }
                    break;
                case LOCATION_AND_NAME_DIFFER_PROMOTION_TYPE: // 011 -- name change & location change.
                    if (infoForMerge.getNameChangedFlag()
                            && infoForMerge.getLocationChangedFlag()
                            && !infoForMerge.getCreatedOnBranchFlag()) {
                        typeOfMerge = mergeType;
                        break outerLoop;
                    }
                    break;
                default:
                    throw new QVCSException("Could not deduce the type of merge for file: [" + shortWorkfileName + "]");
            }
        }
        return typeOfMerge;
    }

    /**
     * Read data from a FileInputStream into a byte[]. The caller must first create the byte[] to be the size of the data to be read. This method will then
     * chunk up the read so as to avoid out-of-memory problems.
     * @param data the buffer into which the data will be read.
     * @param inputStream the stream from which to read the data.
     * @throws IOException if the read fails.
     */
    public static void readDataFromStream(byte[] data, FileInputStream inputStream) throws IOException {
        int offset = 0;

        while (true) {
            int bytesLeft = data.length - offset;
            if (bytesLeft > QVCSConstants.BYTES_TO_XFER) {
                int bytesToRead = QVCSConstants.BYTES_TO_XFER;
                int bytesRead = inputStream.read(data, offset, bytesToRead);
                offset += bytesRead;
            } else {
                int bytesToRead = bytesLeft;
                inputStream.read(data, offset, bytesToRead);
                break;
            }
        }
    }

    /**
     * Write data from a byte[] to a FileOutputStream.
     * @param data the data to write.
     * @param outputStream the output stream to write the data to.
     * @throws IOException if the write fails.
     */
    public static void writeDataToStream(byte[] data, FileOutputStream outputStream) throws IOException {
        int offset = 0;

        while (true) {
            int bytesLeft = data.length - offset;
            if (bytesLeft > QVCSConstants.BYTES_TO_XFER) {
                int bytesToWrite = QVCSConstants.BYTES_TO_XFER;
                outputStream.write(data, offset, bytesToWrite);
                offset += bytesToWrite;
            } else {
                int bytesToWrite = bytesLeft;
                outputStream.write(data, offset, bytesToWrite);
                break;
            }
        }
    }

    /**
     * Read a file into a buffer; returning the buffer.
     * @param fileToRead the file to read.
     * @return a byte[] containing the file contents.
     * @throws IOException if file does not exist, or cannot be read.
     */
    public static byte[] readFileToBuffer(File fileToRead) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(fileToRead);
        byte[] buffer = new byte[(int) fileToRead.length()];
        Utility.readDataFromStream(buffer, fileInputStream);
        return buffer;
    }

    /**
     * Compare digests.
     * @param digestA the first digest.
     * @param digestB the second digest.
     * @return true if the digests match; false if they do not match.
     */
    public static boolean digestsMatch(byte[] digestA, byte[] digestB) {
        boolean retVal = true;

        if ((digestA != null) && (digestB != null) && (digestA.length == digestB.length)) {
            int count = digestA.length;
            for (int i = 0; i < count; i++) {
                if (digestA[i] != digestB[i]) {
                    LOGGER.trace("digest non-match in loop at index [{}]", i);
                    retVal = false;
                    break;
                }
            }
        } else {
            LOGGER.trace("digest non-match, NO loop needed");
            retVal = false;
        }
        return retVal;
    }

}
