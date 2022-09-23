package content.adContent.gui;//package gui.contentPanel.ADConnection;
//
//import gui.Dialogs;
//import utils.TimeTransformations;
//
//import javax.naming.directory.BasicAttribute;
//import javax.naming.directory.DirContext;
//import javax.naming.directory.ModificationItem;
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.util.Calendar;
//import java.util.HashMap;
//import java.util.Objects;
//
///**
// * Erstellt eine GUI mit der man einen Mitarbeiter bearbeiten kann
// */
//public class EditUser extends UserGui {
//
//    final Modifications modifications;
//
//    public EditUser(User user, Content content) {
//        super(user, content, "Veränderung von einem Mitarbeiter");
//
//        //ADD LINES
//        for (UserAtt userAtt : UserAtt.values()) {
//            Line line = new Line(userAtt);
//            //Listeners for the Lines. This checks immediately after any change and sets the correct Input accordingly!
//            line.addChangeListener(e -> {
//                if (line.getUserAtt().isEnum()) {
//                    line.correctInput = !Objects.equals(line.getInput(), UserAtt.NICHT_GESETZT);
//                } else if (line.getUserAtt().isDate()) {
//                    //do nothing here input is always correct
//                } else if (line.getUserAtt().isBoolean()) {
//                    //do nothing here input is always correct
//                } else {
//                    if (userAtt == UserAtt.Disziplinarischer_Vorgesetzter) {
//                        try {
//                            String racf = content.getRacf(line.getInput());
//                            if (racf.length() > 0) {
//                                line.setInput(racf);
//                                line.correctInput = true;
//                            } else
//                                line.correctInput = false;
//                        } catch (IllegalStateException exception) {
//                            line.correctInput = false;
//                            Dialogs.errorBox("Es gibt mehr als einen User mit diesem Namen, Zuordnung nicht möglich. Bitte direkt das gesuchte RACF eingeben", "Error finding specific User");
//                        }
//                    } else {
//                        line.correctInput = user.getString(line.getUserAtt()).equals(line.getInput()) || UserAtt.inputCorrect(line.getUserAtt(), line.getInput()) || line.getInput().equals("");
//                    }
//                }
//            });
//
//        }
//        for (UserAtt userAtt : UserAtt.getUneditableUserAtt())
//            lines.get(userAtt).setEditable(false);
//        addLines();
//
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosed(WindowEvent e) {
//                super.windowClosed(e);
//                user.gettingEdited = false;
//            }
//        });
//
//        modifications = new Modifications();
//        user.gettingEdited = true;
//
//        pack();
//        setVisible(true);
//    }
//
//    @Override
//    protected ActionListener okButtonActionListener() {
//        return e -> {
//            if (bestaetigung() && modifications.commitChanges())
//                dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
//        };
//    }
//
//    /**
//     * Erlaub setzten von einem Monat in der Vergangenheit, bis zu zwei Jahren Voraus
//     * {@inheritDoc}
//     */
//    @Override
//    protected DateSelectionConstraint dateConstrainsVertragsende() {
//        return model -> {
//            Calendar cal = Calendar.getInstance();
//            //letzer monat ist im vorjahr
//            if (cal.get(Calendar.MONTH) == Calendar.JANUARY) {
//                return (cal.get(Calendar.YEAR) == model.getYear() + 1 && model.getMonth() == 12 ||
//                        cal.get(Calendar.YEAR) == model.getYear() ||
//                        cal.get(Calendar.YEAR) == model.getYear() - 1 ||
//                        cal.get(Calendar.YEAR) == model.getYear() - 2 && model.getMonth() == Calendar.JANUARY);
//            } else
//                return (cal.get(Calendar.YEAR) == model.getYear() && cal.get(Calendar.MONTH) - 1 <= model.getMonth() ||
//                        cal.get(Calendar.YEAR) == model.getYear() - 1 ||
//                        cal.get(Calendar.YEAR) == model.getYear() - 2 && model.getMonth() <= cal.get(Calendar.MONTH));
//        };
//    }
//
//    public boolean bestaetigung() {
//        String[] options = new String[]{"Speichern", "Cancel"};
//        int option = JOptionPane.showOptionDialog(null, modifications.changePanel(), "Mitarbeiter verändern?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
//                null, options, options[1]);
//        return option == 0;
//    }
//
//    class Modifications {
//        JPanel jPanel;
//        final GridBagLayout changeGridBag = new GridBagLayout();
//        final GridBagConstraints changeC = new GridBagConstraints();
//        final HashMap<UserAtt, ModificationItem> modificationItemList = new HashMap<>();
//
//        public Modifications() {
//            for (Line line : getComponentes()) {
//                modificationItemList.put(line.getUserAtt(), null);
//                if (line.getUserAtt().isDate()) {
//                    line.addChangeListener(e -> {
//                        if (TimeTransformations.jDateToString((JDatePicker) line.getInputComponent()).equals(user.getString(line.getUserAtt())))
//                            modificationItemList.put(line.getUserAtt(), null);
//                        else
//                            modificationItemList.put(line.getUserAtt(), getModificationItem(true, line.getUserAtt().getSearchAtt(), TimeTransformations.jDatetoAD((JDatePicker) line.getInputComponent())));
//                        setButtonAfterChange();
//                    });
//                } else if (line.getUserAtt().isBoolean()) {
//                    line.addChangeListener(e -> {
//                        String selected = line.getUserAtt().getStringfromBoolean(((JCheckBox) line.getInputComponent()).getModel().isSelected());
//
//                        if (user.get(line.getUserAtt()).equals(selected))
//                            modificationItemList.put(line.getUserAtt(), null);
//                        else
//                            modificationItemList.put(line.getUserAtt(), getModificationItem(true, line.getUserAtt().getSearchAtt(), selected));
//
//                        setButtonAfterChange();
//                    });
//                } else
//                    line.addChangeListener(e -> {
//                        if (!user.hasAtt(line.getUserAtt()) && (!Objects.equals(line.getInput(), UserAtt.NICHT_GESETZT) && !Objects.equals(line.getInput(), " "))) {
//                            modificationItemList.put(line.getUserAtt(), getModificationItem(false, line.getUserAtt().getSearchAtt(), line.getInput()));
//                        } else if (user.hasAtt(line.getUserAtt()) && !Objects.equals(line.getInput(), user.get(line.getUserAtt()))) {
//                            modificationItemList.put(line.getUserAtt(), getModificationItem(true, line.getUserAtt().getSearchAtt(), line.getInput()));
//                        } else {
//                            modificationItemList.put(line.getUserAtt(), null);
//                        }
//                        setButtonAfterChange();
//                    });
//            }
//        }
//
//        private void setButtonAfterChange() {
//            okButton.setEnabled(hasChanges());
//            if (!okButton.isEnabled()) {
//                if (!hasChanges())
//                    okButton.setToolTipText("<html> Es gibt keine Veränderungen! </html>");
//                StringBuilder sb = new StringBuilder();
//                sb.append("<html>").append("Folgende Felder sind noch nicht korrekt gesetzt:").append("<br>");
//                for (Line comp : getComponentes()) {
//                    if (!comp.correctInput)
//                        sb.append("- ").append(comp.getUserAtt()).append("<br>");
//                }
//                sb.append("</html>");
//                okButton.setToolTipText(sb.toString());
//            } else {
//                okButton.setToolTipText("<html> Veränderungen speichern </html>");
//            }
//        }
//
//        private ModificationItem getModificationItem(boolean replace, String searchAtt, String input) {
//            return new ModificationItem(replace ? DirContext.REPLACE_ATTRIBUTE : DirContext.ADD_ATTRIBUTE, new BasicAttribute(searchAtt, input));
//        }
//
//        private boolean hasChanges() {
//            if (!isAllInputCorrect())
//                return false;
//            for (Line line : getComponentes()) {
//                if (modificationItemList.get(line.getUserAtt()) != null)
//                    return true;
//            }
//            return false;
//        }
//
//        public JPanel changePanel() {
//            jPanel = new JPanel();
//            changeC.gridx = 2;
//            jPanel.setLayout(changeGridBag);
//            modificationItemList.forEach((userAtt, modificationItem) -> {
//                if (modificationItem != null) {
//                    changeC.gridwidth = GridBagConstraints.REMAINDER;
//                    JLabel jLabel = new JLabel(userAtt + ": " + user.getString(userAtt) + " → " + getComponentes().stream().filter(line -> line.getUserAtt() == userAtt).map(Line::getInput).findFirst().orElse(UserAtt.UNBEKANNT));
//                    changeGridBag.setConstraints(jLabel, changeC);
//                    jPanel.add(jLabel);
//                }
//            });
//            return jPanel;
//        }
//
//        private boolean commitChanges() {
//            if (content.changeUser(user, getModificationArray())) {
//                try {
//                    modificationItemList.forEach((userAtt, modificationItem) -> {
//                        if (modificationItem != null)
//                            user.set(userAtt, getComponentes().stream().filter(line -> line.getUserAtt() == userAtt).map(Line::getInput).findFirst().orElse(UserAtt.UNBEKANNT));
//
//                    });
//                } catch (Exception e) {
//                    Dialogs.errorBox(e.getMessage(), "ERROR COMMITTING CHANGES TO AD");
//                    return false;
//                }
//                return true;
//            }
//            return false;
//        }
//
//        private ModificationItem[] getModificationArray() {
//            ModificationItem[] temp = modificationItemList.values().toArray(new ModificationItem[0]);
//            int modifications = 0;
//            for (ModificationItem modificationItem : temp)
//                if (modificationItem != null)
//                    modifications++;
//            ModificationItem[] modificationItems = new ModificationItem[modifications];
//            int counter = 0;
//            for (ModificationItem modificationItem : temp)
//                if (modificationItem != null) {
//                    modificationItems[counter++] = modificationItem;
//                }
//            return modificationItems;
//        }
//
//    }
//
//}
