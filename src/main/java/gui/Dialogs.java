package gui;

import gui.extras.ProgressBar;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class Dialogs {
    public static void errorBox(String infoMessage, String titleBar) {
        if (infoMessage.length() > 250) {
            JPanel jPanel = new JPanel();
            jPanel.setPreferredSize(new Dimension(1000, 500));
            JTextArea jTextArea = new JTextArea();
            jTextArea.setWrapStyleWord(true);
            jTextArea.setLineWrap(true);
            jTextArea.append(infoMessage);
            JScrollPane jScrollPane = new JScrollPane(jTextArea);
            jPanel.setLayout(new BorderLayout());
            jPanel.add(jScrollPane, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(null, jPanel, titleBar, JOptionPane.WARNING_MESSAGE);
        } else
            JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.WARNING_MESSAGE);
    }

    public static void infoBox(JComponent parent, String infoMessage, String titleBar) {
        if (infoMessage.length() > 250) {
            JPanel jPanel = new JPanel();
            jPanel.setPreferredSize(new Dimension(1000, 500));
            JTextArea jTextArea = new JTextArea();
            jTextArea.setWrapStyleWord(true);
            jTextArea.setLineWrap(true);
            jTextArea.append(infoMessage);
            JScrollPane jScrollPane = new JScrollPane(jTextArea);
            jPanel.setLayout(new BorderLayout());
            jPanel.add(jScrollPane, BorderLayout.CENTER);
            JOptionPane.showMessageDialog(parent, jPanel, titleBar, JOptionPane.INFORMATION_MESSAGE);
        } else
            JOptionPane.showMessageDialog(parent, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);

    }

    public static String[] pwBox() {
        JPanel panel = new JPanel();
        JLabel labeln = new JLabel("RACF:");
        JLabel labelp = new JLabel("PW:");
        JTextField textField = new JTextField(10);
        JPasswordField pass = new JPasswordField(10);
        panel.add(labeln, BorderLayout.CENTER);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(labelp, BorderLayout.SOUTH);
        panel.add(pass, BorderLayout.SOUTH);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null, panel, "AD ADMIN LOGIN", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        if (option == 0) {
            if (Objects.equals(textField.getText(), "") || pass.getPassword().length == 0)
                return new String[]{"", "", "Forgot"};
            return new String[]{textField.getText(), String.valueOf(pass.getPassword()), "Full"};
        } else
            return new String[]{"", "", "Cancel"};
    }

    public static ProgressBar loadingBox(int maxValue) {
        return new ProgressBar(maxValue);
    }

    public static void infoBox(String infoMessage, String titleBar) {
        infoBox(null, infoMessage, titleBar);
    }
}
