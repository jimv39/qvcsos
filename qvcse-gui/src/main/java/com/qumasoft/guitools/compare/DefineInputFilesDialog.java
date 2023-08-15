/*   Copyright 2004-2023 Jim Voris
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
package com.qumasoft.guitools.compare;

import com.qumasoft.guitools.AbstractQVCSCommandDialog;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.border.TitledBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Define input files dialog.
 * @author Jim Voris
 */
public class DefineInputFilesDialog extends AbstractQVCSCommandDialog {
    private static final long serialVersionUID = 977626325175209187L;

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(DefineInputFilesDialog.class);
    private boolean successFlag = true;
    private boolean ignoreAllWhiteSpaceFlag;
    private boolean ignoreLeadingWhiteSpaceFlag;
    private boolean ignoreCaseFlag = false;
    private final Frame parentFrame;

    /**
     * Define the input files dialog.
     * @param parent the parent frame.
     */
    public DefineInputFilesDialog(CompareFrame parent) {
        super(parent, true);
        // <editor-fold>
        getContentPane().setLayout(new BorderLayout(5, 5));
        setSize(430, 300);
        setVisible(false);
        m_NorthSpacerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        getContentPane().add(BorderLayout.NORTH, m_NorthSpacerPanel);
        m_NorthSpacerPanel.setBounds(0, 0, 430, 10);
        m_SouthSpacerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        getContentPane().add(BorderLayout.SOUTH, m_SouthSpacerPanel);
        m_SouthSpacerPanel.setBounds(0, 219, 430, 10);
        m_WestSpacerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        getContentPane().add(BorderLayout.WEST, m_WestSpacerPanel);
        m_WestSpacerPanel.setBounds(0, 15, 10, 199);
        m_EastSpacerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        getContentPane().add(BorderLayout.EAST, m_EastSpacerPanel);
        m_EastSpacerPanel.setBounds(420, 15, 10, 199);
        m_MainCenterPanel.setLayout(new BorderLayout(5, 5));
        getContentPane().add(BorderLayout.CENTER, m_MainCenterPanel);
        m_MainCenterPanel.setBounds(15, 15, 400, 199);
        m_ButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        m_MainCenterPanel.add(BorderLayout.SOUTH, m_ButtonPanel);
        m_ButtonPanel.setBounds(0, 164, 400, 35);
        m_CompareButton.setText("Compare");
        m_CompareButton.setMnemonic((int) 'C');
        m_ButtonPanel.add(m_CompareButton);
        m_CompareButton.setBounds(230, 5, 87, 25);
        m_CancelButton.setText("Cancel");
        m_CancelButton.setMnemonic((int) 'A');
        m_ButtonPanel.add(m_CancelButton);
        m_CancelButton.setBounds(322, 5, 73, 25);
        m_ActionParentPanel.setLayout(new GridLayout(2, 1, 0, 0));
        m_MainCenterPanel.add(BorderLayout.CENTER, m_ActionParentPanel);
        m_ActionParentPanel.setBounds(0, 0, 400, 159);
        m_DefineFileNamesPanel.setLayout(new BoxLayout(m_DefineFileNamesPanel, BoxLayout.Y_AXIS));
        m_DefineFileNamesPanel.setBorder(new TitledBorder("Define file names:"));
        m_ActionParentPanel.add(m_DefineFileNamesPanel);
        m_DefineFileNamesPanel.setBounds(0, 0, 400, 50);
        m_FirstNamePanel.setLayout(new BoxLayout(m_FirstNamePanel, BoxLayout.X_AXIS));
        m_DefineFileNamesPanel.add(m_FirstNamePanel);
        m_FirstNamePanel.setBounds(0, 54, 400, 54);
        m_FirstFileTextField.setAlignmentY(0.0F);
        m_FirstFileTextField.setAlignmentX(0.0F);
        m_FirstNamePanel.add(m_FirstFileTextField);
        m_FirstFileTextField.setBounds(0, 0, 321, 25);
        m_FirstFileTextField.setMaximumSize(new Dimension(321, 25));
        m_BrowseForFirstFile.setText("Browse");
        m_BrowseForFirstFile.setAlignmentY(0.0F);
        m_FirstNamePanel.add(m_BrowseForFirstFile);
        m_BrowseForFirstFile.setBounds(321, 0, 79, 25);
        m_SecondNamePanel.setLayout(new BoxLayout(m_SecondNamePanel, BoxLayout.X_AXIS));
        m_DefineFileNamesPanel.add(m_SecondNamePanel);
        m_SecondNamePanel.setBounds(0, 0, 400, 54);
        m_SecondFileTextField.setAlignmentY(0.0F);
        m_SecondFileTextField.setAlignmentX(0.0F);
        m_SecondNamePanel.add(m_SecondFileTextField);
        m_SecondFileTextField.setBounds(0, 0, 321, 25);
        m_SecondFileTextField.setMaximumSize(new Dimension(321, 25));
        m_BrowseForSecondFile.setText("Browse");
        m_BrowseForSecondFile.setAlignmentY(0.0F);
        m_SecondNamePanel.add(m_BrowseForSecondFile);
        m_BrowseForSecondFile.setBounds(321, 0, 79, 25);
        m_DefineOptionsPanel.setLayout(new GridLayout(3, 1, 0, 0));
        m_DefineOptionsPanel.setBorder(new TitledBorder("Select options:"));
        m_ActionParentPanel.add(m_DefineOptionsPanel);
        m_DefineOptionsPanel.setBounds(0, 79, 400, 79);
        m_IgnoreAllWhiteSpaceRadioButton.setText("Ignore all whitespace");
        m_IgnoreAllWhiteSpaceRadioButton.setMnemonic((int) 'G');
        m_DefineOptionsPanel.add(m_IgnoreAllWhiteSpaceRadioButton);
        m_IgnoreAllWhiteSpaceRadioButton.setBounds(0, 0, 400, 26);
        m_IgnoreLeadingWhiteSpaceRadioButton.setText("Ignore leading whitespace");
        m_IgnoreLeadingWhiteSpaceRadioButton.setMnemonic((int) 'R');
        m_DefineOptionsPanel.add(m_IgnoreLeadingWhiteSpaceRadioButton);
        m_IgnoreLeadingWhiteSpaceRadioButton.setBounds(0, 26, 400, 26);
        m_IgnoreCaseCheckBox.setText("Ignore case");
        m_IgnoreCaseCheckBox.setMnemonic((int) 'C');
        m_DefineOptionsPanel.add(m_IgnoreCaseCheckBox);
        m_IgnoreCaseCheckBox.setBounds(0, 52, 400, 26);
        setTitle("Define Input Files");
        setResizable(false);
        setModal(true);
        // </editor-fold>

        //{{REGISTER_LISTENERS
        SymWindow aSymWindow = new SymWindow();
        this.addWindowListener(aSymWindow);
        //}}

        // Add our button listeners
        m_CancelButton.addActionListener(new CancelButtonListener());
        m_BrowseForSecondFile.addActionListener(new BrowseForFile2Listener());
        m_BrowseForFirstFile.addActionListener(new BrowseForFile1Listener());
        m_CompareButton.addActionListener(new CompareButtonListener());
        m_IgnoreAllWhiteSpaceRadioButton.addActionListener(new IgnoreAllWhiteSpaceRadioButtonListener());
        m_IgnoreLeadingWhiteSpaceRadioButton.addActionListener(new IgnoreLeadingWhiteSpaceRadioButtonListener());

        parentFrame = parent;

        // Initialize the controls to the property settings.
        m_FirstFileTextField.setText(parent.getRemoteProperties().getMRUFile1Name("", ""));
        m_SecondFileTextField.setText(parent.getRemoteProperties().getMRUFile2Name("", ""));

        center();
    }

    @Override
    public void dismissDialog() {
        successFlag = false;
        setVisible(false);
    }

    @Override
    public void addNotify() {
        // Record the size of the window prior to calling parents addNotify.
        Dimension d = getSize();

        super.addNotify();

        if (fComponentsAdjusted) {
            return;
        }

        // Adjust components according to the insets
        Insets ins = getInsets();
        setSize(ins.left + ins.right + d.width, ins.top + ins.bottom + d.height);
        Component components[] = getContentPane().getComponents();
        for (Component component : components) {
            Point p = component.getLocation();
            p.translate(ins.left, ins.top);
            component.setLocation(p);
        }
        fComponentsAdjusted = true;
    }
    // Used for addNotify check.
    boolean fComponentsAdjusted = false;

    class SymWindow extends java.awt.event.WindowAdapter {

        @Override
        public void windowClosing(java.awt.event.WindowEvent event) {
            Object object = event.getSource();
            if (object == DefineInputFilesDialog.this) {
                DefineInputFilesDialog_WindowClosing(event);
            }
        }
    }

    void DefineInputFilesDialog_WindowClosing(java.awt.event.WindowEvent event) {
        setVisible(false);
    }

    String getFile1Name() {
        return m_FirstFileTextField.getText();
    }

    String getFile2Name() {
        return m_SecondFileTextField.getText();
    }

    boolean getSuccess() {
        return successFlag;
    }

    boolean getIgnoreAllWhiteSpace() {
        return ignoreAllWhiteSpaceFlag;
    }

    boolean getIgnoreLeadingWhiteSpace() {
        return ignoreLeadingWhiteSpaceFlag;
    }

    boolean getIgnoreCase() {
        return ignoreCaseFlag;
    }

    boolean getIgnoreEOLChanges() {
        return false;
    }

    String dirNameFromFilename(String fullFileName) {
        int lastDirSeparator = fullFileName.lastIndexOf(System.getProperty("file.separator"));
        String dirName;
        if (lastDirSeparator > 0) {
            dirName = fullFileName.substring(0, lastDirSeparator);
            LOGGER.trace(dirName);
        } else {
            dirName = "";
        }
        return dirName;
    }
    //{{DECLARE_CONTROLS
    javax.swing.JPanel m_NorthSpacerPanel = new javax.swing.JPanel();
    javax.swing.JPanel m_SouthSpacerPanel = new javax.swing.JPanel();
    javax.swing.JPanel m_WestSpacerPanel = new javax.swing.JPanel();
    javax.swing.JPanel m_EastSpacerPanel = new javax.swing.JPanel();
    javax.swing.JPanel m_MainCenterPanel = new javax.swing.JPanel();
    javax.swing.JPanel m_ButtonPanel = new javax.swing.JPanel();
    javax.swing.JButton m_CompareButton = new javax.swing.JButton();
    javax.swing.JButton m_CancelButton = new javax.swing.JButton();
    javax.swing.JPanel m_ActionParentPanel = new javax.swing.JPanel();
    javax.swing.JPanel m_DefineFileNamesPanel = new javax.swing.JPanel();
    javax.swing.JPanel m_SecondNamePanel = new javax.swing.JPanel();
    javax.swing.JTextField m_SecondFileTextField = new javax.swing.JTextField();
    javax.swing.JButton m_BrowseForSecondFile = new javax.swing.JButton();
    javax.swing.JPanel m_FirstNamePanel = new javax.swing.JPanel();
    javax.swing.JTextField m_FirstFileTextField = new javax.swing.JTextField();
    javax.swing.JButton m_BrowseForFirstFile = new javax.swing.JButton();
    javax.swing.JPanel m_DefineOptionsPanel = new javax.swing.JPanel();
    javax.swing.JRadioButton m_IgnoreAllWhiteSpaceRadioButton = new javax.swing.JRadioButton();
    javax.swing.JRadioButton m_IgnoreLeadingWhiteSpaceRadioButton = new javax.swing.JRadioButton();
    javax.swing.JCheckBox m_IgnoreCaseCheckBox = new javax.swing.JCheckBox();
    //}}

    class CancelButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            successFlag = false;
            setVisible(false);
        }
    }

    class BrowseForFile1Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            FileDialog getFileName = new FileDialog(parentFrame, "Select File 1");
            getFileName.setMode(FileDialog.LOAD);
            getFileName.setModal(true);
            getFileName.setDirectory(dirNameFromFilename(m_FirstFileTextField.getText()));
            getFileName.setVisible(true);
            if (getFileName.getFile() != null) {
                m_FirstFileTextField.setText(getFileName.getDirectory() + getFileName.getFile());
            }
        }
    }

    class BrowseForFile2Listener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            FileDialog getFileName = new FileDialog(parentFrame, "Select File 2");
            getFileName.setMode(FileDialog.LOAD);
            getFileName.setModal(true);
            getFileName.setDirectory(dirNameFromFilename(m_SecondFileTextField.getText()));
            getFileName.setVisible(true);
            if (getFileName.getFile() != null) {
                m_SecondFileTextField.setText(getFileName.getDirectory() + getFileName.getFile());
            }
        }
    }

    class CompareButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            successFlag = true;
            ignoreAllWhiteSpaceFlag = m_IgnoreAllWhiteSpaceRadioButton.isSelected();
            ignoreLeadingWhiteSpaceFlag = m_IgnoreLeadingWhiteSpaceRadioButton.isSelected();
            ignoreCaseFlag = m_IgnoreCaseCheckBox.isSelected();
            setVisible(false);
        }
    }

    class IgnoreLeadingWhiteSpaceRadioButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            m_IgnoreAllWhiteSpaceRadioButton.setSelected(false);
        }
    }

    class IgnoreAllWhiteSpaceRadioButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            m_IgnoreLeadingWhiteSpaceRadioButton.setSelected(false);
        }
    }

}
