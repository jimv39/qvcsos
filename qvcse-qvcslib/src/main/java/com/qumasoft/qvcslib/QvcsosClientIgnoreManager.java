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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client side ignore manager. This singleton class is where we figure out whether to ignore a directory or a file, based on entries in the project's .qvcsosignore file.
 * Unlike git, there can only be a single .qvcsosignore file for a project. It is located in the project root directory, and there is a separate one for each project.
 * If no .qvcsosignore file exists for a project, then none of the files in the project tree will be ignored. (Note there is a separate user preference that allows the client to
 * ignore so-called hidden files.
 *
 * @author Jim Voris
 */
public final class QvcsosClientIgnoreManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(QvcsosClientIgnoreManager.class);

    /**
     * This is a singleton
     */
    private static final QvcsosClientIgnoreManager QVCSOS_CLIENT_IGNORE_MANAGER = new QvcsosClientIgnoreManager();

    private static final String QVCSOS_IGNORE_FILENAME = ".qvcsosignore";
    private final Map<String, Set<String>> ignoredDirectorySegmentsMap;
    private final Map<String, Set<String>> ignoredFilesMap;

    /**
     * Get the Directory contents manager factory singleton.
     *
     * @return the Directory contents manager factory singleton.
     */
    public static QvcsosClientIgnoreManager getInstance() {
        return QVCSOS_CLIENT_IGNORE_MANAGER;
    }

    private QvcsosClientIgnoreManager() {
        // These maps are keyed by the ProjectRoot value... that is, the name of the directory which contains the .qvcsosignore file.
        // The maps contain the set of directories and files to ignore that get populated (lazily) from the projects' respective .qvcsosignore file.
        ignoredDirectorySegmentsMap = Collections.synchronizedMap(new TreeMap<>());
        ignoredFilesMap = Collections.synchronizedMap(new TreeMap<>());
    }

    /**
     * Determine if the given directory should be ignored.Look at the current copy of .qvcsosignore to decide if the requested directory should be ignored.
     * @param fullDirectoryName the canonical path for the directory to check.
     * @param appendedPath the appended path of the given directory.
     * @return true if it should be ignored; false otherwise.
     * @throws java.io.IOException
     */
    public boolean ignoreDirectory(String fullDirectoryName, String appendedPath) throws IOException {
        LOGGER.debug("Ignore this directory [{}]; project appended path: [{}]", fullDirectoryName, appendedPath);
        String projectRoot = fullDirectoryName.substring(0, fullDirectoryName.length() - appendedPath.length());
        projectRoot = Utility.endsWithPathSeparator(projectRoot);
        LOGGER.debug("Ignore directory project root: [{}]", projectRoot);
        String ignoreFileFullPath = projectRoot + QVCSOS_IGNORE_FILENAME;
        LOGGER.debug("Prospective full path to .qvcsosignore file: [{}]", ignoreFileFullPath);
        Set<String> ignoreDirectorySet = ignoredDirectorySegmentsMap.get(projectRoot);
        if (null == ignoreDirectorySet) {
            IgnoreFileData ignoreFileData = readIgnoreFile(ignoreFileFullPath);
            ignoreDirectorySet = ignoreFileData.getDirectorySet();
            ignoredDirectorySegmentsMap.put(projectRoot, ignoreDirectorySet);
            ignoredFilesMap.put(projectRoot, ignoreFileData.getFileSet());
        }
        return ignoreDirectoryAlgorithm(ignoreDirectorySet, projectRoot, fullDirectoryName);
    }

    /**
     * Determine if the given directory should be ignored.Look at the current copy of .qvcsosignore to decide if the requested directory should be ignored.
     * @param currentWorkfileDirectory the current workfile directory.
     * @param appendedPath the appended path.
     * @param directoryName the name of the directory to evaluate.
     * @return true if it should be ignored; false otherwise.
     * @throws java.io.IOException
     */
    public boolean ignoreDirectoryForDirectoryAdd(File currentWorkfileDirectory, String appendedPath, String directoryName) throws IOException {
        LOGGER.debug("Ignore this directory [{}]; project appended path: [{}]; file directory: [{}]", currentWorkfileDirectory.getCanonicalPath(), appendedPath, directoryName);
        LOGGER.debug("Directory path to test: [{}]", currentWorkfileDirectory.getCanonicalPath() + File.separator + directoryName);
        String projectRoot = currentWorkfileDirectory.getCanonicalPath().substring(0, currentWorkfileDirectory.getCanonicalPath().length() - appendedPath.length());
        projectRoot = Utility.endsWithPathSeparator(projectRoot);
        LOGGER.debug("Ignore directory project root: [{}]", projectRoot);
        String ignoreFileFullPath = projectRoot + QVCSOS_IGNORE_FILENAME;
        LOGGER.debug("Prospective full path to .qvcsosignore file: [{}]", ignoreFileFullPath);
        Set<String> ignoreDirectorySet = ignoredDirectorySegmentsMap.get(projectRoot);
        if (null == ignoreDirectorySet) {
            IgnoreFileData ignoreFileData = readIgnoreFile(ignoreFileFullPath);
            ignoreDirectorySet = ignoreFileData.getDirectorySet();
            ignoredDirectorySegmentsMap.put(projectRoot, ignoreDirectorySet);
            ignoredFilesMap.put(projectRoot, ignoreFileData.getFileSet());
        }
        return ignoreDirectoryAlgorithm(ignoreDirectorySet, projectRoot, currentWorkfileDirectory.getCanonicalPath() + File.separator + directoryName);
    }

    /**
     * Determine if the given file should be ignored.
     * @param appendedPath the appended path for the project containing this file.
     * @param file the file in question.
     * @return true if the file should be ignored; false otherwise.
     * @throws java.io.IOException
     */
    public boolean ignoreFile(String appendedPath, File file) throws IOException {
        LOGGER.debug("IgnoreFile: ignore this file [{}]; project appended path: [{}]; file directory: [{}]", file.getCanonicalPath(), appendedPath, file.getParentFile().getCanonicalPath());
        String projectRoot = file.getParentFile().getCanonicalPath().substring(0, file.getParentFile().getCanonicalPath().length() - appendedPath.length());
        projectRoot = Utility.endsWithPathSeparator(projectRoot);
        LOGGER.debug("Project root: [{}]", projectRoot);
        String ignoreFileFullPath = projectRoot + QVCSOS_IGNORE_FILENAME;
        LOGGER.debug("Prospective full path to .qvcsosignore file: [{}]", ignoreFileFullPath);
        Set<String> ignoreFileSet = ignoredFilesMap.get(projectRoot);
        Set<String> ignoreDirectorySet = ignoredDirectorySegmentsMap.get(projectRoot);
        if (null == ignoreFileSet) {
            IgnoreFileData ignoreFileData = readIgnoreFile(ignoreFileFullPath);
            ignoreDirectorySet = ignoreFileData.getDirectorySet();
            ignoredDirectorySegmentsMap.put(projectRoot, ignoreDirectorySet);
            ignoreFileSet = ignoreFileData.getFileSet();
            ignoredFilesMap.put(projectRoot, ignoreFileSet);
        }
        return ignoreFileAlgorithm(ignoreDirectorySet, ignoreFileSet, file, projectRoot);
    }

    /**
     * Get the name of the .qvcsignore file.
     * @return the name of the .qvcsignore file.
     */
    public static String getQvcsosIgnoreFilename() {
        return QVCSOS_IGNORE_FILENAME;
    }

    private IgnoreFileData readIgnoreFile(String ignoreFileFullPath) {
        IgnoreFileData returnData = new IgnoreFileData();
        LOGGER.info("Should read from: [{}]", ignoreFileFullPath);
        Path path = FileSystems.getDefault().getPath(ignoreFileFullPath);
        if (path.toFile().exists()) {
            try {
                Set<String> directorySet = new HashSet<>();
                Set<String> fileSet = new HashSet<>();
                List<String> fileLines = Files.readAllLines(path);
                fileLines.forEach(line -> {
                    if (line.endsWith(File.separator)) {
                        if (validateDirectoryLine(line)) {
                            directorySet.add(line);
                        } else {
                            LOGGER.info("Invalid directory entry in .qvcsosignore file: [{}], line: [{}]", ignoreFileFullPath, line);
                        }
                    } else {
                        if (validateFileLine(line)) {
                            fileSet.add(line);
                        } else {
                            LOGGER.info("Invalid file entry in .qvcsosignore file: [{}], line: [{}]", ignoreFileFullPath, line);
                        }
                    }
                });
                returnData.setDirectorySet(directorySet);
                returnData.setFileSet(fileSet);
            } catch (IOException ex) {
                LOGGER.warn("Could not read .qvcsosignore file: [{}]", ignoreFileFullPath, ex);
            }
        } else {
            returnData.setDirectorySet(new HashSet<>());
            returnData.setFileSet(new HashSet<>());
        }
        return returnData;
    }

    private boolean validateDirectoryLine(String line) {
        boolean returnFlag = true;
        if (line.length() == 0) {
            returnFlag = false;
        } else {
            LOGGER.debug("Directory line: [{}]", line);
        }
        return returnFlag;
    }

    private boolean validateFileLine(String line) {
        boolean returnFlag = true;
        if (line.length() == 0) {
            returnFlag = false;
        } else {
            LOGGER.debug("File line: [{}]", line);
        }
        return returnFlag;
    }

    private boolean ignoreFileAlgorithm(Set<String> ignoreDirectorySet, Set<String> ignoreFileSet, File file, String projectRoot) throws IOException {
        boolean returnFlag = false;
        String fileCanonicalName = file.getCanonicalPath();
        String filenameWithinProject = fileCanonicalName.substring(projectRoot.length() - 1);
        LOGGER.debug("Full file name: [{}] Path relative file name: [{}]", fileCanonicalName, filenameWithinProject);
        for (String ignoreFileEntry : ignoreFileSet) {
            if (ignoreFileEntry.startsWith("/")) {
                // File we're testing must be anchored to project root.
                if (ignoreFileEntry.endsWith("*")) {
                    String entryMinusStar = ignoreFileEntry.substring(0, ignoreFileEntry.length() - 1);
                    // It must match exactly up to the length of the ignore file entry.
                    if (entryMinusStar.length() <= filenameWithinProject.length()) {
                        String shorterFilenameWithinProject = filenameWithinProject.substring(0, entryMinusStar.length());
                        if (0 == shorterFilenameWithinProject.compareTo(entryMinusStar)) {
                            returnFlag = true;
                            break;
                        }
                    }
                } else {
                    // This has to be an exact match.
                    if (0 == ignoreFileEntry.compareTo(filenameWithinProject)) {
                        returnFlag = true;
                        break;
                    }
                }
            } else {
                // File we're testing could be anywhere and must match the ignore string.
                String[] fileSegments = fileCanonicalName.split(File.separator);
                if (ignoreFileEntry.endsWith("*")) {
                    String entryMinusStar = ignoreFileEntry.substring(0, ignoreFileEntry.length() - 1);
                    if (fileSegments[fileSegments.length - 1].startsWith(entryMinusStar)) {
                        returnFlag = true;
                        break;
                    }
                } else {
                    if (0 == fileSegments[fileSegments.length - 1].compareTo(ignoreFileEntry)) {
                        returnFlag = true;
                        break;
                    }
                }
            }
        }

        // If the returnFlag is still false, check the directory set to see if we should ignore this file.
        if (!returnFlag) {
            returnFlag = ignoreDirectoryAlgorithm(ignoreDirectorySet, projectRoot, file.getParentFile().getCanonicalPath());
        }
        return returnFlag;
    }

    private boolean ignoreDirectoryAlgorithm(Set<String> ignoreDirectorySet, String projectRoot, String fullDirectoryName) {
        boolean returnFlag = false;
        String directoryNameWithinProject = fullDirectoryName.substring(projectRoot.length() - 1);
        directoryNameWithinProject = Utility.endsWithPathSeparator(directoryNameWithinProject);
        LOGGER.debug("Full directory name: [{}] Path relative directory name: [{}]", fullDirectoryName, directoryNameWithinProject);
        for (String ignoreDirectoryEntry : ignoreDirectorySet) {
            if (ignoreDirectoryEntry.startsWith("/")) {
                // Directory we're testing must be anchored to project root.
                // This has to be an exact match for the length of the entry
                if (directoryNameWithinProject.length() >= ignoreDirectoryEntry.length()) {
                    if (0 == ignoreDirectoryEntry.compareTo(directoryNameWithinProject.substring(0, ignoreDirectoryEntry.length()))) {
                        returnFlag = true;
                        break;
                    }
                }
            } else {
                // Directory we're testing could be anywhere and must contain the ignore string.
                String adjustedDirectoryEntry = File.separator + ignoreDirectoryEntry;
                if (directoryNameWithinProject.contains(adjustedDirectoryEntry)) {
                    returnFlag = true;
                    break;
                }
            }
        }
        return returnFlag;
    }
}
