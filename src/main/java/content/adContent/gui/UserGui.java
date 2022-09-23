package content.adContent.gui;


import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import content.adContent.content.ADContent;
import content.adContent.content.User;
import content.adContent.content.enums.UserAtt;
import util.TimeTransformations;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


/**
 * Überklasse für Userbearbeitung/Erstellung. Ein User sollte nur gespeichert werden können, wenn savable true ist
 */
public abstract class UserGui extends JFrame {
    protected final User user;
    protected final ADContent content;
    protected final HashMap<UserAtt, Line> lines = new HashMap<>();

    protected final JPanel jPanel = new JPanel();
    private final JPanel jPanel_Lines = new JPanel();
    protected final JButton okButton = new JButton("OK");
    protected final JButton cancelButton = new JButton("Cancel");

    public UserGui(User user, ADContent content, String title) {
        //INIT FRAME
        setTitle(title);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.setResizable(true);

        //CONTENT
        jPanel.setLayout(new BorderLayout());
        jPanel.add(jPanel_Lines, BorderLayout.CENTER);
        JPanel jPanel_Buttons = new JPanel();
        jPanel.add(jPanel_Buttons, BorderLayout.SOUTH);
        this.add(jPanel);
        this.user = user;
        this.content = content;

        //BUTTONS
        okButton.addActionListener(okButtonActionListener());
        jPanel_Buttons.add(okButton);
        cancelButton.addActionListener(e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));
        jPanel_Buttons.add(cancelButton);
        okButton.setEnabled(false);

        //GENERAL KEY LISTENER
        ActionListener escListener = e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        ActionListener enterListener = okButtonActionListener();
        getRootPane().registerKeyboardAction(escListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        getRootPane().registerKeyboardAction(enterListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Get all {@link JComponent}s in this GUI
     *
     * @return an ArrayList with all components
     */
    public List<Line> getComponentes() {
        return new ArrayList<>(lines.values());
    }

    /**
     * Checks all Inputs
     *
     * @return {@code True} if all Inputs are Correct, otherwise {@code false}
     */
    protected boolean isAllInputCorrect() {
        return getComponentes().stream().allMatch(line -> line.correctInput);
    }

    /**
     * What should happen if the ok-button is pressed
     *
     * @return an ActionListener
     */
    abstract protected ActionListener okButtonActionListener();

    /**
     * Constrains for the DatePicker Vertragsende
     *
     * @return The Constraints
     */
    abstract protected DatePickerSettings dateConstrainsVertragsende();
    //LocalDate.now().minusDays(365),LocalDate.now().plusDays(365)

    /**
     * Filles the GUI with all Lines
     */
    protected void addLines() {
        jPanel_Lines.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridLayout gridLayout = new GridLayout(lines.size(), 2, 10, 10);
        jPanel_Lines.setLayout(gridLayout);
        Line line;
        for (UserAtt userAtt : UserAtt.values()) {
            if ((line = lines.get(userAtt)) != null) {
                jPanel_Lines.add(line.label);
                jPanel_Lines.add(line.input);
            }
        }
    }

    /**
     * One Line of Data in the Panel
     */
    protected class Line {
        private final JComponent input;
        private final JLabel label;
        protected boolean correctInput;
        private final UserAtt userAtt;
        private final List<ChangeListener> changeListeners = new ArrayList<>();

        public Line(UserAtt userAtt, String data, boolean optional, boolean loadData) {
            this.userAtt = userAtt;
            correctInput = loadData || optional;

            //GenLabel
            label = new JLabel(optional ? "(Optional) " + userAtt : userAtt.toString());

            //GenInput
            if (userAtt.isEnum()) {
                input = new JComboBox<>(Objects.requireNonNull(userAtt.getEnum()));
                JComboBox<?> temp = ((JComboBox<?>) input);
                temp.addItemListener(e -> changeListeners.forEach(changeListener -> changeListener.stateChanged(new ChangeEvent(temp))));
                if (loadData)
                    temp.setSelectedItem(userAtt.getEnumfromString(data).toString());
            } else if (userAtt.isDate()) {
                DatePickerSettings dateSettings = new DatePickerSettings();
                dateSettings.setFormatForDatesCommonEra("dd.MM.yyyy");
                DatePicker datePicker = new DatePicker(dateSettings);
                datePicker.setDateToToday();
                input = datePicker;
                datePicker.addDateChangeListener(e -> changeListeners.forEach(changeListener -> changeListener.stateChanged(new ChangeEvent(datePicker))));
                if (userAtt == UserAtt.Vertragsende) {
                    dateSettings.setDateRangeLimits(dateConstrainsVertragsende().getDateRangeLimits().firstDate, dateConstrainsVertragsende().getDateRangeLimits().lastDate);
                    correctInput = true;
                }
                if (loadData) {
                    //Wir müssen beim Vertragsende gucken, ob der user keins hat (das ist aber dann im ad als 25.07.30828) gespeichert.
                    //Damit das aber nicht im DatePicker in dem GUI steht schreiben wir keins rein, und setzten es erst, wenn der OK button gedrückt wird.
                    LocalDate l = TimeTransformations.stringToLocalDate(user.get(UserAtt.Vertragsende));
                    if (userAtt == UserAtt.Vertragsende) {
                        if (l != null)
                            datePicker.setDate(l);
                    } else
                        datePicker.setDate(l);
                }
            } else if (userAtt.isBoolean()) {
                input = new JCheckBox();
                ((JCheckBox) input).addItemListener(e -> changeListeners.forEach(changeListener -> changeListener.stateChanged(new ChangeEvent(this))));
                if (loadData)
                    ((JCheckBox) input).getModel().setSelected(Objects.requireNonNull(userAtt.getBooleanfromString(user.get(userAtt))));
            } else {
                input = new JTextField(20);
                JTextField temp = ((JTextField) input);
                temp.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        changeListeners.forEach(changeListener -> changeListener.stateChanged(new ChangeEvent(this)));
                    }
                });
                //Schreibe "Nicht Gesetzt" ins Feld wenn es leer ist und entferne, wenn es editiert wird

                input.addFocusListener(new FocusAdapter() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        if (temp.isEditable() && temp.getText().equals(UserAtt.NICHT_GESETZT))
                            temp.setText("");
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        if (temp.isEditable() && temp.getText().equals(""))
                            temp.setText(UserAtt.NICHT_GESETZT);
                    }
                });
                if (loadData)
                    temp.setText(data == null || data.equals("") ? UserAtt.NICHT_GESETZT : data);
            }

            //Set Color WHITE on focus; Set Color RED if wrong input on focusLost
            input.addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(FocusEvent e) {
                    input.putClientProperty("JComponent.outline", "");
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (!correctInput)
                        input.putClientProperty("JComponent.outline", "error");
                }
            });
            lines.put(userAtt, this);
        }

        /**
         * This will always init a line with empty content, if optional is true there will be an additional tag in the Label
         *
         * @param userAtt  The Attribute this line is created for
         * @param optional if the line is optional
         */
        public Line(UserAtt userAtt, boolean optional) {
            this(userAtt, null, optional, false);
        }

        /**
         * This will init a line with the Content from the given User
         *
         * @param userAtt The Attribute this line is created for
         */
        public Line(UserAtt userAtt) {
            this(userAtt, user.get(userAtt), false, true);
        }

        /**
         * Transforms the Input of this line into a String for the {@link User}
         *
         * @return the Input as a String
         */
        public String getInput() {
            if (userAtt.isEnum())
                //this should never be null, because there is always an item selected?
                return Objects.requireNonNull(((JComboBox<?>) input).getSelectedItem()).toString();
            if (userAtt.isDate())
                return ((DatePicker) input).getText();
            if (userAtt.isBoolean())
                return String.valueOf(((JCheckBox) input).getModel().isSelected());
            return ((JTextField) input).getText();
        }

        public void setEditable(Boolean editable) {
            if (input instanceof JTextField)
                ((JTextField) input).setEditable(false);
            else
                input.setEnabled(editable);
        }

        public void setColor(Color color) {
            this.input.setBackground(color);
        }

        public UserAtt getUserAtt() {
            return userAtt;
        }

        public JComponent getInputComponent() {
            return input;
        }

        /**
         * What should happen if the input of a line is changed
         *
         * @param changeListener the Listener that will be called if this line changes
         */
        public void addChangeListener(ChangeListener changeListener) {
            changeListeners.add(changeListener);
        }

        @Override
        public String toString() {
            return "Line{" +
                    "input=" + getInput() +
                    ", correctInput=" + correctInput +
                    ", userAtt=" + userAtt +
                    '}';
        }


        public void setInput(String racf) {
            if (this.input instanceof JTextField)
                ((JTextField) this.input).setText(racf);

            else
                throw new UnsupportedOperationException();


        }
    }


    void updateView(JPanel jPanel) {
        System.out.println("updating view");
        setContentPane(jPanel);
        pack();
    }

    @Override
    public void setEnabled(boolean b) {
        for (Line line : getComponentes())
            line.setEditable(b);
        okButton.setVisible(b);
        cancelButton.setVisible(b);
        pack();
        setDefaultCloseOperation(b ? DISPOSE_ON_CLOSE : DO_NOTHING_ON_CLOSE);
    }
}
