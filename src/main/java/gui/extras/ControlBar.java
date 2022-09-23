/*
 * Copyright 2019 FormDev Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gui.extras;

import com.formdev.flatlaf.util.UIScale;

import javax.swing.*;
import java.awt.*;

/**
 * @author Karl Tauber
 */
public class ControlBar extends JPanel {
    private JLabel infoLabel;

    public ControlBar() {
        initComponents();

        UIScale.addPropertyChangeListener(e -> {
            // update info label because user scale factor may change
            updateInfoLabel();
        });
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (infoLabel != null) updateInfoLabel();
    }

    private void updateInfoLabel() {
        String javaVendor = System.getProperty("java.vendor");
        if ("Oracle Corporation".equals(javaVendor)) javaVendor = null;
        double systemScaleFactor = UIScale.getSystemScaleFactor(getGraphicsConfiguration());
        float userScaleFactor = UIScale.getUserScaleFactor();
        Font font = UIManager.getFont("Label.font");
        String newInfo = "(Java " + System.getProperty("java.version") + (javaVendor != null ? ("; " + javaVendor) : "") + (systemScaleFactor != 1 ? (";  system scale factor " + systemScaleFactor) : "") + (userScaleFactor != 1 ? (";  user scale factor " + userScaleFactor) : "") + (systemScaleFactor == 1 && userScaleFactor == 1 ? "; no scaling" : "") + "; " + font.getFamily() + " " + font.getSize() + (font.isBold() ? " BOLD" : "") + (font.isItalic() ? " ITALIC" : "") + ")";

        if (!newInfo.equals(infoLabel.getText())) infoLabel.setText(newInfo);
    }

    private void enabledDisable(Container container, boolean enabled) {
        for (Component c : container.getComponents()) {
            if (c instanceof JPanel) {
                enabledDisable((JPanel) c, enabled);
                continue;
            }

            c.setEnabled(enabled);

            if (c instanceof JScrollPane) {
                Component view = ((JScrollPane) c).getViewport().getView();
                if (view != null) view.setEnabled(enabled);
            } else if (c instanceof JTabbedPane) {
                JTabbedPane tabPane = (JTabbedPane) c;
                int tabCount = tabPane.getTabCount();
                for (int i = 0; i < tabCount; i++) {
                    Component tab = tabPane.getComponentAt(i);
                    if (tab != null) tab.setEnabled(enabled);
                }
            }

            if (c instanceof JToolBar) enabledDisable((JToolBar) c, enabled);
        }
    }


    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        infoLabel = new JLabel();

        //======== this ========
//        setLayout(new MigLayout("insets dialog",
//                // columns
//                "[fill]" + "[fill]" + "[fill]" + "[grow,fill]" + "[button,fill]",
//                // rows
//                "[bottom]" + "[]"));


        //---- infoLabel ----//
        infoLabel.setText("text");
        add(infoLabel, "cell 1 1,alignx center,growx 0");

    }

}
