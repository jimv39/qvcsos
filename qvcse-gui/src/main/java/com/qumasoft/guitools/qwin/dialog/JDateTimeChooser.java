/*   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.guitools.qwin.dialog;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * A date/time chooser panel.
 * @author Jim Voris
 */
public class JDateTimeChooser extends javax.swing.JPanel {
    private static final long serialVersionUID = 7045062870751237617L;

    private static final int ROW_COUNT = 7;
    private static final int COLUMN_COUNT = 7;
    private static final int PREFERRED_COLUMN_WIDTH = 25;
    private Date chooserDate;
    private Calendar chooserCalendar;
    private int intDayOfMonth = 1;
    private final DateTableModel dateTableModel;
    private final DateFormatSymbols dateFormatSymbols;
    private String[] daysOfWeek;

    /**
     * Create a date/time chooser. The date defaults to 'now'.
     */
    JDateTimeChooser() {
        this(new Date());
    }

    /**
     * Create a date/time chooser with the given date as the default 'value'.
     * @param date
     */
    JDateTimeChooser(Date date) {
        this.dateFormatSymbols = new DateFormatSymbols();
        this.dateTableModel = new DateTableModel();
        initComponents();
        chooserDate = date;
        populateComponents();
    }

    /**
     * Get the date.
     * @return the date.
     */
    Date getDate() {
        return chooserCalendar.getTime();
    }

    /**
     * Set the date.
     * @param date the date.
     */
    void setDate(Date date) {
        chooserDate = date;
        chooserCalendar.setTime(date);
    }

    private void populateComponents() {
        daysOfWeek = dateFormatSymbols.getWeekdays();

        chooserCalendar = Calendar.getInstance();
        chooserCalendar.setTimeInMillis(chooserDate.getTime());
        intDayOfMonth = chooserCalendar.get(Calendar.DAY_OF_MONTH);

        dateTable.setModel(dateTableModel);
        dateTable.setDefaultRenderer(javax.swing.JLabel.class, new CellRenderer());
        dateTable.setTableHeader(null);

        // Set the column width to 25 pixels wide
        for (int i = 0; i < COLUMN_COUNT; i++) {
            TableColumn column = dateTable.getColumnModel().getColumn(i);
            int width = PREFERRED_COLUMN_WIDTH;
            column.setPreferredWidth(width);
        }

        // Only allow a single cell to be selected.
        dateTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dateTable.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        initSelectionListener();

        monthComboBox.setModel(new MonthComboBoxModel());
        monthComboBox.setSelectedIndex(chooserCalendar.get(Calendar.MONTH));

        populateTimeSpinner();
        populateYearSpinner();

        selectDateCell(chooserCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private void populateYearSpinner() {
        // One year in the future.
        // <editor-fold>
        Date maxDate = new Date(chooserDate.getTime() + (1000L * 60L * 60L * 24L * 365L));

        // 10 years in the past.
        Date minDate = new Date(chooserDate.getTime() - (1000L * 60L * 60L * 24L * 365L * 10L));
        // </editor-fold>
        SpinnerDateModel spinnerDateModel = new SpinnerDateModel(chooserCalendar.getTime(), minDate, maxDate, Calendar.YEAR);
        yearSpinner.setModel(spinnerDateModel);

        // Define the editor so we'll show only the year.
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(yearSpinner, "yyyy");
        yearSpinner.setEditor(dateEditor);

        dateEditor.getTextField().addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                int keyCode = e.getKeyCode();
                switch (keyCode) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT: {
                        break;
                    }
                    default: {
                        e.consume();
                    }
                }
            }
        });
    }

    private void populateTimeSpinner() {
        SpinnerDateModel spinnerDateModel = new SpinnerDateModel(chooserCalendar.getTime(), null, null, Calendar.MINUTE);
        timeSpinner.setModel(spinnerDateModel);

        // Define the editor so we'll show only the time.
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "hh:mm:ss aa");
        timeSpinner.setEditor(timeEditor);

        timeEditor.getTextField().addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                int keyCode = e.getKeyCode();
                switch (keyCode) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT: {
                        break;
                    }
                    default: {
                        e.consume();
                    }
                }
            }
        });
    }

    private void initSelectionListener() {
        TableSelectionListener tableSelectionListener = new TableSelectionListener();

        // Listen for row changes.
        dateTable.getSelectionModel().addListSelectionListener(tableSelectionListener);

        // Listen for column changes.
        dateTable.getColumnModel().getSelectionModel().addListSelectionListener(tableSelectionListener);
    }

    private void selectCell(int rowIndex, int columnIndex) {
        dateTable.changeSelection(rowIndex, columnIndex, false, false);
    }

    private void selectDateCell(int dayOfMonthValue) {
        // Find the cell for the given day of month
        // We'll do this brute force, for now
        boolean continueFlag = true;
        DateTableModel dataModel = (DateTableModel) dateTable.getModel();
        for (int row = 1; continueFlag && (row < ROW_COUNT); row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                JLabel dayOfMonth = (JLabel) dataModel.getValueAt(row, column);
                if (dayOfMonth.getText().length() > 0) {
                    int dom = Integer.valueOf(dayOfMonth.getText()).intValue();
                    if (dom == dayOfMonthValue) {
                        selectCell(row, column);
                        continueFlag = false;
                        break;
                    }
                }
            }
        }
    }

    private String computeDayOfMonthAtRowAndColumn(int rowIndex, int columnIndex) {
        // Default to an empty string (for beginning and end of month).
        String returnValue = "";

        // Figure out the month index...
        int monthIndex = columnIndex + ((rowIndex - 1) * COLUMN_COUNT);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(chooserCalendar.getTime());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int dayOfWeekOfFirst = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int dayOfMonthIndex = monthIndex - dayOfWeekOfFirst;
        int dayOfMonth = dayOfMonthIndex + 1;
        int year = calendar.get(Calendar.YEAR);

        if (monthIndex >= dayOfWeekOfFirst) {
            returnValue = Integer.toString(dayOfMonth);
        }

        int month = calendar.get(Calendar.MONTH);
        switch (month) {
            case Calendar.JANUARY:
            case Calendar.MARCH:
            case Calendar.MAY:
            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.OCTOBER:
            case Calendar.DECEMBER:
                if (dayOfMonth > 31) {
                    returnValue = "";
                }
                break;
            case Calendar.APRIL:
            case Calendar.JUNE:
            case Calendar.SEPTEMBER:
            case Calendar.NOVEMBER:
                if (dayOfMonth > 30) {
                    returnValue = "";
                }
                break;
            default:
            case Calendar.FEBRUARY:
                if ((year % 4) == 0) {
                    if (dayOfMonth > 29) {
                        returnValue = "";
                    }
                } else {
                    if (dayOfMonth > 28) {
                        returnValue = "";
                    }
                }
                break;
        }

        return returnValue;
    }

    class TableSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent listSelectionEvent) {
            if (listSelectionEvent.getValueIsAdjusting()) {
                return;
            }
            ListSelectionModel listSelectionModel = (ListSelectionModel) listSelectionEvent.getSource();
            DateTableModel dataModel = (DateTableModel) dateTable.getModel();
            if (!listSelectionModel.isSelectionEmpty()) {
                if (listSelectionEvent.getFirstIndex() >= 0) {
                    int selectedRow = dateTable.getSelectedRow();
                    if (selectedRow > 0) {
                        JLabel dayOfMonth = (JLabel) dataModel.getValueAt(selectedRow, dateTable.getSelectedColumn());
                        if (dayOfMonth.getText().length() > 0) {
                            intDayOfMonth = Integer.valueOf(dayOfMonth.getText()).intValue();
                            chooserCalendar.set(Calendar.DAY_OF_MONTH, intDayOfMonth);
                        } else {
                            selectDateCell(intDayOfMonth);
                        }
                    } else {
                        selectDateCell(intDayOfMonth);
                    }
                }
            }
        }
    }

    class DateTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private static final int SUNDAY_COLUMN_INDEX = 0;
        private static final int MONDAY_COLUMN_INDEX = 1;
        private static final int TUESDAY_COLUMN_INDEX = 2;
        private static final int WEDNESDAY_COLUMN_INDEX = 3;
        private static final int THURSDAY_COLUMN_INDEX = 4;
        private static final int FRIDAY_COLUMN_INDEX = 5;
        private static final int SATURDAY_COLUMN_INDEX = 6;

        JLabel m_ValueAtLabel = new JLabel();

        public DateTableModel() {
            super();
        }

        @Override
        public int getRowCount() {
            return ROW_COUNT;
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        /**
         * Returns the lowest common denominator Class in the column. This is used by the table to set up a default renderer and editor for the column.
         *
         * @return the common ancestor class of the object values in the model.
         */
        @Override
        public Class getColumnClass(int columnIndex) {
            return javax.swing.JLabel.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex == 0) {
                switch (columnIndex) {
                    case SUNDAY_COLUMN_INDEX:
                        m_ValueAtLabel.setText(daysOfWeek[Calendar.SUNDAY].substring(0, 1));
                        break;
                    case MONDAY_COLUMN_INDEX:
                        m_ValueAtLabel.setText(daysOfWeek[Calendar.MONDAY].substring(0, 1));
                        break;
                    case TUESDAY_COLUMN_INDEX:
                        m_ValueAtLabel.setText(daysOfWeek[Calendar.TUESDAY].substring(0, 1));
                        break;
                    case WEDNESDAY_COLUMN_INDEX:
                        m_ValueAtLabel.setText(daysOfWeek[Calendar.WEDNESDAY].substring(0, 1));
                        break;
                    case THURSDAY_COLUMN_INDEX:
                        m_ValueAtLabel.setText(daysOfWeek[Calendar.THURSDAY].substring(0, 1));
                        break;
                    case FRIDAY_COLUMN_INDEX:
                        m_ValueAtLabel.setText(daysOfWeek[Calendar.FRIDAY].substring(0, 1));
                        break;
                    case SATURDAY_COLUMN_INDEX:
                    default:
                        m_ValueAtLabel.setText(daysOfWeek[Calendar.SATURDAY].substring(0, 1));
                        break;
                }
            } else {
                m_ValueAtLabel.setText(computeDayOfMonthAtRowAndColumn(rowIndex, columnIndex));
            }
            return m_ValueAtLabel;
        }
    }

    class CellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private static final long serialVersionUID = -8022231309078117375L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel inputLabel = (JLabel) dateTableModel.getValueAt(row, column);
            String text = inputLabel.getText();

            // Set the column alignment.
            switch (column) {
                default:
                    setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                    break;
            }
            if (isSelected && (text.length() > 0)) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                super.setForeground(table.getForeground());
                super.setBackground(table.getBackground());
            }

            setFont(table.getFont());

            if (hasFocus && (row != 0) && (text.length() > 0)) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                if (table.isCellEditable(row, column)) {
                    super.setForeground(UIManager.getColor("Table.focusCellForeground"));
                    super.setBackground(UIManager.getColor("Table.focusCellBackground"));
                }
            } else {
                setBorder(noFocusBorder);
            }

            setText(text);
            setIcon(null);

            if (row == 0) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            }
            return this;
        }
    }

    class MonthComboBoxModel extends DefaultComboBoxModel<String> {
        private static final long serialVersionUID = 1L;

        public MonthComboBoxModel() {
            String[] months = dateFormatSymbols.getMonths();
            for (int i = 0; i < 12; i++) {
                addElement(months[i]);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        datePanel = new javax.swing.JPanel();
        monthYearPanel = new javax.swing.JPanel();
        monthComboBox = new javax.swing.JComboBox();
        yearSpinner = new javax.swing.JSpinner();
        dateTablePanel = new javax.swing.JPanel();
        dateTable = new javax.swing.JTable();
        timePanel = new javax.swing.JPanel();
        timeSpinner = new javax.swing.JSpinner();

        setLayout(new java.awt.BorderLayout(5, 5));

        datePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true), "Date"));
        datePanel.setLayout(new java.awt.BorderLayout(0, 5));

        monthYearPanel.setLayout(new java.awt.BorderLayout(5, 0));

        monthComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        monthComboBox.setToolTipText("Choose month");
        monthComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                monthComboBoxItemStateChanged(evt);
            }
        });
        monthYearPanel.add(monthComboBox, java.awt.BorderLayout.WEST);

        yearSpinner.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        yearSpinner.setToolTipText("Choose year");
        yearSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                yearSpinnerStateChanged(evt);
            }
        });
        monthYearPanel.add(yearSpinner, java.awt.BorderLayout.EAST);

        datePanel.add(monthYearPanel, java.awt.BorderLayout.NORTH);

        dateTablePanel.setLayout(new java.awt.BorderLayout());

        dateTable.setModel(new DateTableModel());
        dateTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        dateTable.setAutoscrolls(false);
        dateTable.setCellSelectionEnabled(true);
        dateTable.setDoubleBuffered(true);
        dateTable.setShowHorizontalLines(false);
        dateTable.setShowVerticalLines(false);
        dateTablePanel.add(dateTable, java.awt.BorderLayout.CENTER);

        datePanel.add(dateTablePanel, java.awt.BorderLayout.CENTER);

        add(datePanel, java.awt.BorderLayout.NORTH);

        timePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true), "Time"));
        timePanel.setLayout(new java.awt.BorderLayout(0, 5));

        timeSpinner.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        timeSpinner.setToolTipText("Choose time");
        timeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                timeSpinnerStateChanged(evt);
            }
        });
        timePanel.add(timeSpinner, java.awt.BorderLayout.CENTER);

        add(timePanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    private void monthComboBoxItemStateChanged(java.awt.event.ItemEvent evt)//GEN-FIRST:event_monthComboBoxItemStateChanged
    {//GEN-HEADEREND:event_monthComboBoxItemStateChanged
        int index = monthComboBox.getSelectedIndex();
        chooserCalendar.set(Calendar.MONTH, index);
        dateTableModel.fireTableDataChanged();
        selectDateCell(chooserCalendar.get(Calendar.DAY_OF_MONTH));
    }//GEN-LAST:event_monthComboBoxItemStateChanged

    private void timeSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_timeSpinnerStateChanged
    {//GEN-HEADEREND:event_timeSpinnerStateChanged
        Date time = (Date) timeSpinner.getModel().getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        chooserCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        chooserCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
        chooserCalendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND));
    }//GEN-LAST:event_timeSpinnerStateChanged

    private void yearSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_yearSpinnerStateChanged
    {//GEN-HEADEREND:event_yearSpinnerStateChanged
        Date year = (Date) yearSpinner.getModel().getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(year);
        chooserCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        dateTableModel.fireTableDataChanged();
        selectDateCell(chooserCalendar.get(Calendar.DAY_OF_MONTH));
    }//GEN-LAST:event_yearSpinnerStateChanged
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel datePanel;
    private javax.swing.JTable dateTable;
    private javax.swing.JPanel dateTablePanel;
    private javax.swing.JComboBox monthComboBox;
    private javax.swing.JPanel monthYearPanel;
    private javax.swing.JPanel timePanel;
    private javax.swing.JSpinner timeSpinner;
    private javax.swing.JSpinner yearSpinner;
// End of variables declaration//GEN-END:variables
}
