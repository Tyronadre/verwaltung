package gui.extras;

import util.StaticHelper;

import javax.swing.*;
import java.awt.*;

public class ProgressBar extends JPanel {
    final JProgressBar jProgressBar;
    final JLabel jLabel_InfoText;
    final JLabel jLabel_Progess;
    final int max;

    public ProgressBar(int max) {
        setMinimumSize(new Dimension(300, 100));

        JPanel jPanel = new JPanel();
        jProgressBar = new JProgressBar();
        jProgressBar.setMinimum(0);
        jProgressBar.setMaximum(max);
        jLabel_InfoText = new JLabel("");
        jLabel_Progess = new JLabel("0%");

        jPanel.setLayout(new BorderLayout());
        JPanel jPanel_top = new JPanel();
        jPanel_top.setLayout(new BorderLayout());
        jPanel_top.add(jLabel_InfoText,BorderLayout.WEST);
        jPanel_top.add(jLabel_Progess, BorderLayout.EAST);
        jPanel.add(jPanel_top, BorderLayout.NORTH);
        jPanel.add(jProgressBar, BorderLayout.CENTER);
        add(jPanel);

        this.max = max;

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        setVisible(true);
    }

    public void updateBar(String newString, int newValue, boolean indeterminateMode) {
        jLabel_InfoText.setText(newString);
        jProgressBar.setValue(newValue);
        jProgressBar.setIndeterminate(indeterminateMode);
        if (newValue > max)
            jLabel_Progess.setText(100 + "%");
        jLabel_Progess.setText(StaticHelper.round(newValue / (double) max * 100,2) + "%");

        repaint();
        revalidate();
    }

}
