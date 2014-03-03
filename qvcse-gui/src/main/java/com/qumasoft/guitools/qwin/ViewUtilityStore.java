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

import com.qumasoft.qvcslib.Utility;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * View Utility store. Holds the information about what utility to use for viewing files of given file extensions.
 *
 * @author Jim Voris
 */
public class ViewUtilityStore implements Serializable {

    private static final long serialVersionUID = -8036099849675704175L;

    /**
     * This map contains the file extensions and their associated command line.
     */
    private final Map<String, String> commandLineMap = Collections.synchronizedMap(new TreeMap<String, String>());
    /**
     * This set contains commands that have been used. These are not associated with any specific file or extension, but serve as a quick way for the user to re-use commands that
     * they have used in the past.
     */
    private final Set<String> commandLineSet = Collections.synchronizedSet(new TreeSet<String>());

    /**
     * Creates a new instance of AuthenticationStore.
     */
    ViewUtilityStore() {
    }

    String[] getViewUtilityCommandLine(String fullWorkfileName) {
        // Look to see if there is a command for this file's extension.
        String extension = Utility.getFileExtension(fullWorkfileName);
        String command = commandLineMap.get(extension);
        String[] commandLine;

        // If the command line is still null, we need to ask the user to provide
        // a command for us.
        if (command == null) {
            commandLine = getCommandLine(fullWorkfileName, extension);
        } else {
            commandLine = new String[2];
            commandLine[0] = command;
            commandLine[1] = fullWorkfileName;
        }
        return commandLine;
    }

    private String[] getCommandLine(String fullWorkfileName, String extension) {
        String[] existingCommands = getExistingCommands();
        GetViewUtilityCommandDialog getUtilityCommandDialog = new GetViewUtilityCommandDialog(QWinFrame.getQWinFrame(), true, existingCommands);
        getUtilityCommandDialog.setVisible(true);
        String[] commandLine = null;

        if (getUtilityCommandDialog.getIsOK()) {
            String selectedUtility = getUtilityCommandDialog.getSelectedUtility();
            if (getUtilityCommandDialog.getUseForFilesOfThisExtensionFlag()) {
                commandLineMap.put(extension, selectedUtility);
            } else {
                commandLineSet.add(selectedUtility);
            }
            commandLine = new String[2];
            commandLine[0] = selectedUtility;
            commandLine[1] = fullWorkfileName;
        }

        return commandLine;
    }

    private String[] getExistingCommands() {
        String[] existingCommands;
        Set<String> existingCommandSet = new TreeSet<>();

        // Get the commands that are associated with a specific extension
        synchronized (commandLineMap) {
            Set keys = commandLineMap.keySet();
            Iterator i = keys.iterator();

            while (i.hasNext()) {
                String key = (String) i.next();
                existingCommandSet.add(commandLineMap.get(key));
            }
        }

        // Get the commands that are not associated with a specific extension
        synchronized (commandLineSet) {
            Iterator<String> i = commandLineSet.iterator();
            while (i.hasNext()) {
                existingCommandSet.add(i.next());
            }
        }

        // Combine the commands into a single String[].
        existingCommands = new String[existingCommandSet.size()];
        Iterator i = existingCommandSet.iterator();
        int j = 0;
        while (i.hasNext()) {
            existingCommands[j++] = (String) i.next();
        }
        return existingCommands;
    }

    String getAssociatedUtility(final String fullWorkfileName) {
        // Look to see if there is a command for this file's extension.
        String extension = Utility.getFileExtension(fullWorkfileName);
        String commandLine = commandLineMap.get(extension);
        return commandLine;
    }

    void removeUtilityAssociation(final String fullWorkfileName) {
        String extension = Utility.getFileExtension(fullWorkfileName);
        commandLineMap.remove(extension);
    }

    void dumpMap() {
        synchronized (commandLineMap) {
            Set keys = commandLineMap.keySet();
            Iterator i = keys.iterator();

            System.err.println("View Utility Commands: ");
            while (i.hasNext()) {
                String key = (String) i.next();
                System.err.println(key + ": " + commandLineMap.get(key));
            }
        }
    }
}
