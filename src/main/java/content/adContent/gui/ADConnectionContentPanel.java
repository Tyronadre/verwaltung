package content.adContent.gui;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.components.FlatButton;
import com.formdev.flatlaf.icons.FlatSearchIcon;
import content.adContent.content.ADContent;
import content.adContent.content.User;
import content.adContent.content.enums.ADServer;
import content.adContent.content.enums.UserAtt;
import gui.Dialogs;
import gui.Gui;
import gui.ContentPanel;
import util.Tasks.Task;
import util.TimeTransformations;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class ADConnectionContentPanel extends ContentPanel {
    ADContent adContent;
    final JTable table;
    JScrollPane jScrollPane;

    DefaultTableModel defaultTableModel;
    final HashMap<String, String> userGettingEdited = new HashMap<>();
    final HashMap<String, ADContent> adContentServer = new HashMap<>();
    private ADServer provider;

    //ControlBar
    JPanel thisTasks;
    JLabel runningTasks;
    JLabel runningBGTasks;
    JPanel bgTasks;
    JProgressBar runningTasksPG;
    JProgressBar runningBGTasksPG;

    //BackgroundTasks
    List<Task> backgroundTasks = new LinkedList<>();

    public ADConnectionContentPanel(Gui gui) {
        super(gui);
        this.setLayout(new OverlayLayout(this));
        if (adContent == null)
            adContent = new ADContent(ADServer.getDefaultServer());

        table = new JTable();
        JTextField tf = new JTextField();
        tf.setEditable(false);
        table.setDefaultEditor(Object.class, null);
        table.addMouseListener(tableOnClickEvent());

        jScrollPane = new JScrollPane(table);
        this.add(jScrollPane);

        provider = ADServer.getDefaultServer();

        updateContent();
    }

    @Override
    public JToolBar getToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setMargin(new Insets(3, 3, 3, 3));

        FlatButton newUserButton = new FlatButton();
        newUserButton.setToolTipText("New User");
        newUserButton.setIcon(new FlatSVGIcon(Objects.requireNonNull(getClass().getResource("/svg/back.svg"))));
        newUserButton.addActionListener(e -> new AddUser(adContent));
        toolBar.add(newUserButton);

        JTextField searchField = new JTextField(20);
        searchField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search");
        searchField.putClientProperty(FlatClientProperties.TEXT_FIELD_LEADING_ICON, new FlatSearchIcon());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                if (searchField.getText().equals(""))
                    update(adContent.getContent());
                else {
                    Set<User> userList = new LinkedHashSet<>();

                    userList.addAll(adContent.searchUserList(UserAtt.Name, searchField.getText()));
                    userList.addAll(adContent.searchUserList(UserAtt.RACF, searchField.getText()));
                    String[][] data = new String[userList.size()][2];
                    User[] temp = userList.toArray(new User[0]);
                    for (int i = 0; i < userList.size(); i++) {
                        data[i][0] = temp[i].getString(UserAtt.RACF);
                        data[i][1] = temp[i].getString(UserAtt.Name);
                    }
                    update(data);
                }
            }
        });
        toolBar.add(searchField);

        return toolBar;
    }

    @Override
    public JPanel getControlPanel() {
        JPanel controlBar = new JPanel();
        thisTasks = new JPanel();
        runningTasks = new JLabel();
        runningBGTasks = new JLabel();
        runningTasksPG = new JProgressBar();
        runningBGTasksPG = new JProgressBar();
        thisTasks.add(runningTasks);
        thisTasks.add(Box.createGlue());
        thisTasks.add(runningTasksPG);

        bgTasks = new JPanel();
        bgTasks.add(runningBGTasks);
        bgTasks.add(Box.createGlue());
        bgTasks.add(runningBGTasksPG);


        controlBar.setLayout(new BorderLayout());
        controlBar.setBorder(new EmptyBorder(5, 5, 5, 5));
        controlBar.add(thisTasks, BorderLayout.WEST);
        controlBar.add(bgTasks, BorderLayout.EAST);
        controlBar.setBorder(new EtchedBorder());

        updateControlBar();
        return controlBar;
    }

    @Override
    public String getTabName() {
        return "AD Connection";
    }

    @Override
    public boolean wantUser() {
        return true;
    }

    @Override
    public void updateControlBar() {
        if (getBackgroundTasks().size() == 0) {
            runningTasks.setText("No Tasks running");
            runningTasksPG.setVisible(false);
        } else {
            runningTasks.setText("Tasks running [" + getBackgroundTasks().size() + "] " + getBackgroundTasks().get(0).getName());
            runningTasksPG.setVisible(true);
        }
        List<Task> otherTasks = gui.getBackgroundTasks();
        otherTasks.removeAll(backgroundTasks);
        if (otherTasks.size() == 0) {
            runningBGTasks.setText("No Background-Tasks running");
            runningBGTasksPG.setVisible(false);
        } else {
            runningBGTasks.setText("Background-Tasks running [" + otherTasks.size() + "] " + otherTasks.get(0).getName());
            runningBGTasksPG.setVisible(true);
        }
    }

    @Override
    public List<Task> getBackgroundTasks() {
        return backgroundTasks;
    }

    @Override
    public JMenu getMenu() {
        JMenu adConnection = new JMenu();
        JMenu serverSel = new JMenu();
        JMenuItem switchUser = new JMenuItem();
        JMenuItem update = new JMenuItem();
        JMenuItem lastUpdate = new JMenuItem();
        adConnection.setText("AD Connection");
        adConnection.setMnemonic('A');

        ADServer[] adServer = ADServer.values();
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButtonMenuItem[] serverSelect = new JRadioButtonMenuItem[adServer.length];
        for (int i = 0; i < adServer.length; i++) {
            serverSelect[i] = new JRadioButtonMenuItem(adServer[i].getName());
            buttonGroup.add(serverSelect[i]);
            int copyi = i;
            serverSelect[i].addActionListener(e -> {
                serverSwitched(adServer[copyi]);
            });
            if (adServer[i].equals(ADServer.getDefaultServer()))
                serverSelect[i].setSelected(true);
            serverSel.add(serverSelect[i]);
        }
        serverSel.setText("AD Server");
        serverSel.setMnemonic('S');
        adConnection.add(serverSel);
//        switchUser.setText("Login with other Credentials");
//        switchUser.addActionListener(this::switchUser);
//        azureConnection.add(switchUser);
//        azureConnection.addSeparator();
        update.setText("Update");
        update.addActionListener(e -> updateContent());
        lastUpdate.setText("Last Sync");
        lastUpdate.addActionListener(this::lastUpdate);
        adConnection.add(lastUpdate);
        return adConnection;
    }

    private ADServer getSelectedServer() {
        return adContent == null ? null : adContent.getSelectedServer();
    }

    private void serverSwitched(ADServer provider) {
        if (adContentServer.containsKey(provider.getName())) {
            this.adContent = adContentServer.get(provider.getName());
        } else {
            this.adContent = new ADContent(provider);
            adContentServer.put(getSelectedServer().getName(), adContent);
        }
        this.provider = provider;
        update(adContent.getContent());
    }

    private MouseListener tableOnClickEvent() {
//        return new MouseAdapter() {
//            @Override
//            public void mousePressed(MouseEvent e) {
//            }
//
//            @Override
//            public void mouseReleased(MouseEvent e) {
//                int row = table.rowAtPoint(e.getPoint());
//                User user = content.getUser((String) table.getModel().getValueAt(row, 0));
//
//                if (table.getModel().getValueAt(row, 0).equals("NO CONTENT"))
//                    return;
//                if (e.isPopupTrigger()) {
//                    table.changeSelection(row, 1, false, false);
//                    openContextMenu(user, e);
//                }
//            }
//
//            @Override
//            public void mouseClicked(MouseEvent e) {
//                int row = table.rowAtPoint(e.getPoint());
//                int column = table.columnAtPoint(e.getPoint());
//                User user = content.getUser((String) table.getModel().getValueAt(row, 0));
//
//                if (table.getModel().getValueAt(row, 0).equals("NO CONTENT"))
//                    return;
//                if (e.isPopupTrigger()) {
//                    openContextMenu(user, e);
//                }
//                if (e.getClickCount() == 2 && !e.isConsumed()) {
//                    e.consume();
//
//                    if (column == 0) {
//                        StringSelection stringSelection = new StringSelection((String) table.getModel().getValueAt(row, 0));
//                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
//                    } else {
//                        editUser(user);
//                    }
//                }
//            }

//            private void openContextMenu(User user, MouseEvent e) {
//                ContextMenuRacfList menu = new ContextMenuRacfList(user, content, RACFList.this);
//                menu.show(e.getComponent(), e.getX(), e.getY());
//            }
//        };
        return null;
    }

    /**
     * UPDATE TABLE MODEL
     * @param list
     */
    private void update(String[][] list) {
        defaultTableModel = new DefaultTableModel(list, adContent.getTitle());
        table.setModel(defaultTableModel);
    }

    /**
     * GET LAST UPDATE
     * @param actionEvent
     */
    private void lastUpdate(ActionEvent actionEvent) {
        Dialogs.infoBox(null, "Last Update from AD: " + TimeTransformations.millisToStringDate(adContent.lastUpdate), "Last Update Info");
    }

    /**
     * SYNC WITH AD AND THEN CALL UPDATE(list)
     */
    private void updateContent() {
        if (!adContent.hasAD()) {
            //TODO BLUR
            setBlur(true);
            JPanel loginInfo = new JPanel();
            loginInfo.setAlignmentX(0.1f);
            loginInfo.setAlignmentY(0.1f);
            loginInfo.add(new JTextField("Not logged in. Please use the button at the top right to choose an Account"));
            this.add(loginInfo);
            jScrollPane.setVisible(false);
        } else {
            if (adContent.updateContent()) {
                jScrollPane.setVisible(true);
                update(adContent.getContent());
            }
        }

    }

    @Override
    public ActionListener getUserActionListener() {
        return e -> {
            String user;
            if (adContent.adConnection.isConnected()) {
                Dialogs.infoBox(this,"You are logged in as " + adContent.getAdmin(), "Current User");
            }
            else if ((user = adContent.adConnection.connect(provider)) != null) {
                updateContent();
                adContent.setAdminUser(user);
            }
        };
    }


}
