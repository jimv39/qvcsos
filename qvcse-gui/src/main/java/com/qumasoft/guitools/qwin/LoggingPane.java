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

import javax.swing.text.Document;

/**
 * The logging pane.
 * @author Jim Voris
 */
public class LoggingPane extends javax.swing.JPanel {
    private static final long serialVersionUID = 4001044130443757642L;

    /**
     * Creates new form LoggingPane.
     */
    public LoggingPane() {
        initComponents();
    }

    void setDocument(Document doc) {
        textArea.setDocument(doc);
    }

    void setFontSize(int fontSize) {
        textArea.setFont(QWinFrame.getQWinFrame().getFont(fontSize));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the FormEditor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        textArea = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        textArea.setEditable(false);
        textArea.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        scrollPane.setViewportView(textArea);

        add(scrollPane);
    }// </editor-fold>//GEN-END:initComponents
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextArea textArea;
// End of variables declaration//GEN-END:variables
}
