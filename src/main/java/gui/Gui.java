package gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.util.LoggingFacade;
import util.Tasks.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.List;

public class Gui extends JFrame {


    private final MenuBar menuBar;
    private final HashMap<Integer, ContentPanel> contentPanel = new HashMap<>();
    private final JTabbedPane tabs = new JTabbedPane();

    private JPanel controlPanel;
    private JToolBar toolbar;


    public Gui() {
        //--- size ---//
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        double scale = dimension.width > 2000 ? 0.75 : 0.5;
        dimension.width *= scale;
        dimension.height *= scale;
        setSize(dimension);


        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(tabs, BorderLayout.CENTER);

        this.menuBar = new MenuBar();
        setIconImage(new ImageIcon(Objects.requireNonNull(getClass().getResource("/png/empolis.png"))).getImage());
    }

    private void about(ActionEvent actionEvent) {
        JLabel titleLabel = new JLabel("IT Verwaltung Empolis");
        titleLabel.putClientProperty(FlatClientProperties.STYLE_CLASS, "h1");

        String link = "https://www.empolis.com";
        JLabel linkLabel = new JLabel("<html><a href=\"#\">" + link + "</a></html>");
        linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        linkLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI(link));
                } catch (IOException | URISyntaxException ex) {
                    JOptionPane.showMessageDialog(linkLabel,
                            "Failed to open '" + link + "' in browser.",
                            "About", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });


        JOptionPane.showMessageDialog(this,
                new Object[]{
                        titleLabel,
                        "GUI with easy register command",
                        " ",
                        "Copyright 2022-" + Year.now() + " Henrik Bornemann @Empolis",
                        linkLabel,
                },
                "About", JOptionPane.PLAIN_MESSAGE);
    }

    public void register(ContentPanel content) throws UnsupportedOperationException {
        if (content == null)
            throw new UnsupportedOperationException();
        contentPanel.put(contentPanel.size(), content);

        tabs.addTab(content.getTabName() == null ? contentPanel.getClass().getName() : content.getTabName(), content);
        if (content.getMenu() != null) {
            menuBar.register(content.getMenu());
        }
        if (content.wantUser())
            menuBar.registerUserButton(content.getUserActionListener());

        int tabIndex = DemoPrefs.getState().getInt(Run.KEY_TAB, 0);
        if (tabIndex >= 0 && tabIndex < tabs.getTabCount())
            switchTab(tabIndex);
    }

    private void switchTab(int newTab) {
        if (contentPanel.get(newTab).getToolbar() != null) {
            if (toolbar != null)
                this.getContentPane().remove(toolbar);
            this.getContentPane().add(toolbar = contentPanel.get(newTab).getToolbar(), BorderLayout.NORTH);
        } else {
            this.getContentPane().remove(toolbar);
            toolbar = null;
        }
        if (contentPanel.get(newTab).getControlPanel() != null) {
            if (controlPanel != null)
                this.getContentPane().remove(controlPanel);
            this.getContentPane().add(controlPanel = contentPanel.get(newTab).getControlPanel(), BorderLayout.SOUTH);
        } else {
            this.getContentPane().remove(controlPanel);
            toolbar = null;
        }
    }

    private void increaseFont(ActionEvent actionEvent) {
        Font font = UIManager.getFont("defaultFont");
        Font newFont = font.deriveFont((float) (font.getSize() + 1));
        UIManager.put("defaultFont", newFont);
        FlatLaf.updateUI();
    }

    private void decreaseFont(ActionEvent actionEvent) {
        Font font = UIManager.getFont("defaultFont");
        Font newFont = font.deriveFont((float) Math.max(font.getSize() - 1, 10));
        UIManager.put("defaultFont", newFont);
        FlatLaf.updateUI();
    }

    private void toggleTheme(ActionEvent actionEvent) {
        FlatAnimatedLafChange.showSnapshot();
        try {
            UIManager.setLookAndFeel(FlatLaf.isLafDark() ? new FlatLightLaf() : new FlatDarkLaf());

        } catch (Exception ex) {
            System.out.println("Failed to create '" + FlatDarkLaf.class + "'.\n" + ex);

            LoggingFacade.INSTANCE.logSevere(null, ex);
        }

        // update all components
        FlatLaf.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    private void close(ActionEvent actionEvent) {
        dispose();
    }

    public List<Task> getBackgroundTasks() {
        List<Task> list = new ArrayList<>();
        for (ContentPanel content : contentPanel.values()) {
            List<Task> bgTasks = content.getBackgroundTasks();
            if (bgTasks != null)
                list.addAll(bgTasks);

        }
        return list;
    }

    class MenuBar extends JMenuBar {
        boolean userButton;
        int addedMenus = 1;

        public MenuBar() {
            //Settings
            {
                JMenu settings = new JMenu();
                settings.setText("Settings");
                settings.setMnemonic('S');

                JCheckBoxMenuItem darkMode = new JCheckBoxMenuItem();
                darkMode.setState(FlatLaf.isLafDark());
                darkMode.setText("Dark Mode");
                darkMode.addActionListener(Gui.this::toggleTheme);
                settings.add(darkMode);

                JMenuItem incFont = new JMenuItem();
                incFont.setText("Increase Font");
                incFont.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                incFont.addActionListener(Gui.this::increaseFont);
                settings.add(incFont);

                JMenuItem decFont = new JMenuItem();
                decFont.setText("Decrease Font");
                decFont.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                decFont.addActionListener(Gui.this::decreaseFont);
                settings.add(decFont);

                settings.addSeparator();

                JMenuItem exit = new JMenuItem();
                exit.setText("Exit");
                exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                exit.setMnemonic('X');
                exit.addActionListener(Gui.this::close);
                settings.add(exit);

                this.add(settings);
            }
            //Help
            {
                JMenu help = new JMenu();
                help.setText("Help");
                help.setMnemonic('H');

                JMenuItem help1 = new JMenuItem();
                help1.setText("Help");
                help1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
                help1.addActionListener(Gui.this::help);
                help.add(help1);

                help.addSeparator();

                JMenuItem about = new JMenuItem();
                about.setText("About");
                about.addActionListener(Gui.this::about);
                help.add(about);

                this.add(help);
            }
            setJMenuBar(this);

        }

        void register(JMenu menu) {
            add(menu, addedMenus++);
        }

        void registerUserButton(ActionListener e) {
            if (!this.userButton) {
                FlatButton userButton = new FlatButton();
                userButton.setIcon(new FlatSVGIcon("svg/person-circle.svg"));
                userButton.setButtonType(FlatButton.ButtonType.toolBarButton);
                userButton.setFocusable(false);
                userButton.addActionListener(e);
                add(Box.createGlue());
                add(userButton);
            } else {
                FlatButton userButton = (FlatButton) getComponent(getMenuCount() - 1);
                userButton.removeActionListener(userButton.getActionListeners()[0]);
                userButton.addActionListener(e);
            }
        }

    }

    private void help(ActionEvent actionEvent) {
    }


}
