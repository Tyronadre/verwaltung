package gui;

import com.formdev.flatlaf.FlatLaf;
import com.github.lgooddatepicker.components.CalendarPanel;
import content.adContent.gui.ADConnectionContentPanel;
import gui.Gui;

import javax.swing.*;

public class Run {
    static final String PREFS_ROOT_PATH = "/flatlaf-demo";
    public static final String KEY_TAB = "tab";


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DemoPrefs.init(PREFS_ROOT_PATH);

            // application specific UI defaults
            FlatLaf.registerCustomDefaultsSource("com.formdev.flatlaf.demo");

            // set look and feel
            DemoPrefs.setupLaf(args);


            // create frame
            CalendarPanel calendarPanel = new CalendarPanel();

            Gui frame = new Gui();
            frame.register(new ADConnectionContentPanel(frame));
//            frame.register(new WebBrowserContentPanel(frame));

            // show frame
//            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
