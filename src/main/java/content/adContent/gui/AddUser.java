package content.adContent.gui;


import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import content.adContent.content.ADContent;
import content.adContent.content.RACFFile;
import content.adContent.content.User;
import content.adContent.content.enums.UserAtt;
import gui.Dialogs;
import gui.extras.ProgressBar;
import util.MultiUserFile;
import util.StaticHelper;
import util.Tasks.ConsoleStreamHandler;
import util.Tasks.ShellRunner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class AddUser extends UserGui implements Runnable {
    final ADContent content;
    final Logger logger = Logger.getLogger("AddUser");

    private ProgressBar progressBar;

    /**
     * Erstellt eine Klasse mit der man einen neuen Mitarbeiter zu diesem {@link ADContent} hinzufügen kann
     *
     * @param content Der Kontext in den der neue Mitarbeiter eingefügt wird
     */
    public AddUser(ADContent content) {
        super(new User(), content, "Erstellen eines neuen Users");
        logger.addHandler(StaticHelper.logToFile());
        this.content = content;

        for (UserAtt userAtt : UserAtt.getNewUserAtt()) {
            Line line = new Line(userAtt, false);
            line.addChangeListener(e -> {
                if (line.getUserAtt().isEnum()) {
                    line.correctInput = !Objects.equals(line.getInput(), UserAtt.NICHT_GESETZT);
                } else if (line.getUserAtt().isDate()) {
                    //do nothing here input is always correct
                } else if (line.getUserAtt().isBoolean()) {
                    //do nothing here input is always correct
                } else {
                    if (userAtt == UserAtt.Disziplinarischer_Vorgesetzter) {
                        try {
                            if (content.hasUser(line.getInput()))
                                line.correctInput = true;
                            else {
                                String racf = content.getRacf(line.getInput());
                                if (racf.length() > 0) {
                                    line.getInputComponent().setToolTipText(line.getInput());
                                    line.setInput(racf);
                                    line.correctInput = true;
                                } else
                                    line.correctInput = false;
                            }
                        } catch (IllegalStateException exception) {
                            line.correctInput = false;
                            Dialogs.errorBox("Es gibt mehr als einen User mit diesem Namen, Zuordnung nicht möglich. Bitte direkt das gesuchte RACF eingeben (Diese Meldung ist ein bisschen kaputt und kann nur mit Leertaste oder Mausklick auf OK geschlossen werden)", "Error finding specific User");
                        }
                    } else
                        line.correctInput = user.getString(line.getUserAtt()).equals(line.getInput()) || UserAtt.inputCorrect(line.getUserAtt(), line.getInput()) || line.getInput().equals("");
                }
                if (line.correctInput)
                    user.set(line.getUserAtt(), line.getInput());
                else
                    user.set(line.getUserAtt(), null);
                if (isAllInputCorrect()) {
                    okButton.setEnabled(true);
                    okButton.setToolTipText("<html>Alle Angaben sind korrekt! </html>");
                } else {
                    okButton.setEnabled(false);
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html> Folgende Felder sind noch nicht korrekt gesetzt:<br>");
                    for (Line line1 : getComponentes())
                        if (!line1.correctInput)
                            sb.append("- ").append(line1.getUserAtt()).append("<br>");
                    sb.append("</html>");
                    okButton.setToolTipText(sb.toString());
                }

            });

        }
        for (UserAtt userAtt : UserAtt.getOptionalNewUserAtt()) {
            Line line = new Line(userAtt, true);
            line.addChangeListener(e -> {
                System.out.println(line.getUserAtt() + " changed!");
                if (line.getUserAtt().isEnum()) {
                    //do nothing here input is always correct
                } else if (line.getUserAtt().isDate()) {
                    //do nothing here input is always correct
                } else if (line.getUserAtt().isBoolean()) {
                    //do nothing here input is always correct
                } else {
                    line.correctInput = UserAtt.inputCorrect(line.getUserAtt(), line.getInput()) || line.getInput().equals("");
                }
                if (line.correctInput)
                    user.set(line.getUserAtt(), line.getInput());

                else
                    user.set(line.getUserAtt(), null);
                okButton.setEnabled(isAllInputCorrect());

            });
        }


        addLines();
        pack();
        setVisible(true);
    }

    /**
     * Der Bestätigungsdialog, wenn man einen neuen User anlegt.
     *
     * @return {@code True} wenn der User angelegt wurde, sonst {@code False}.
     */
    public boolean bestaetigung() {
        String[] options = new String[]{"User erstellen", "Cancel"};
        StringBuilder initData = new StringBuilder();
        initData.append(UserAtt.RACF).append(": \t\t").append(user.get(UserAtt.RACF)).append("\n");
        for (UserAtt att : UserAtt.getNewUserAtt()) {
            initData.append(att).append(": \t\t").append(user.get(att)).append("\n");
        }
        for (UserAtt att : UserAtt.getOptionalNewUserAtt()) {
            if (user.get(att) != null)
                initData.append(att).append(": \t\t").append(user.get(att)).append("\n");
        }
        int option = JOptionPane.showOptionDialog(null, initData.toString(), "Mitarbeiter erstellen?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, null);
        return option == 0;
    }

    @Override
    public void run() {

    }

    @Override
    protected ActionListener okButtonActionListener() {
        return e -> {
            if (isAllInputCorrect()) {
                user.set(UserAtt.RACF, parseRACF());
                if (!MultiUserFile.checkAction(MultiUserFile.Actions.UserCreation, user.get(UserAtt.RACF)) && bestaetigung())
                    addUser();
            }
        };
    }

    @Override
    protected DatePickerSettings dateConstrainsVertragsende() {
        DatePickerSettings datePickerSettings = new DatePicker().getSettings();
        datePickerSettings.setDateRangeLimits(LocalDate.now().minusYears(1), LocalDate.now().plusYears(2));
        return datePickerSettings;
//        return null;
    }

    /**
     * @return RACF zu diesem Namen. Die Nummer entspricht der nächten Nummer, die im {@link ADContent} frei ist.
     */
    private String parseRACF() {String racf_name = parseName();
        List<Integer> givenNumbers = RACFFile.getNumberList(racf_name);
        int racf_num = 1;
        while (givenNumbers.contains(racf_num))
            racf_num++;
        StringBuilder racf = new StringBuilder(String.valueOf(racf_num));
        while (racf.length() < 3)
            racf.insert(0, "0");
        return racf_name + racf;
    }

    /**
     * Entfernt alle Sonderzeichen u.ä. aus dem Namen und gibt den ersten Teil des RACF wieder.
     *
     * @return Den geparsten Namen für das RACF.
     */
    private String parseName() {
        String lastname = user.getString(UserAtt.Nachname).toLowerCase(Locale.ROOT);
        StringBuilder racfname = new StringBuilder();
        while (racfname.length() < 4) {
            if (lastname.length() == 0)
                lastname = user.getString(UserAtt.Vorname);


            //Fälle für ä,ö,ü
            switch (lastname.charAt(0)) {
                case '-':
                    lastname = lastname.substring(1);
                    break;
                case '\u00E4': //ä
                case '\u00C4': //Ä
                    lastname = lastname.substring(1);
                    racfname.append("ae");
                    break;
                case '\u00F6': //ö
                case '\u00D6': //Ö
                    lastname = lastname.substring(1);
                    racfname.append("oe");
                    break;
                case '\u00FC': //ü
                case '\u00DC': //Ü
                    lastname = lastname.substring(1);
                    racfname.append("ue");
                    break;
                case '\u00C9': //É
                case '\u00C8': //È
                case '\u00E9': //é
                case '\u00E8': //è
                    lastname = lastname.substring(1);
                    racfname.append("e");
                    break;
                case '\u00DF':  //ß
                    lastname = lastname.substring(1);
                    racfname.append("ss");
                    break;
                default:
                    if (lastname.startsWith("sch")) { //sch
                        racfname.append("sc");
                        lastname = lastname.substring(3);
                    } else { //Normaler Buchstabe
                        racfname.append(lastname.charAt(0));
                        lastname = lastname.substring(1);
                    }
            }

        }
        return racfname.substring(0, 4).toLowerCase();
    }

    /**
     * Executes the shell runner to add the User created with this Dialog
     */
    private void addUser() {
        MultiUserFile.saveAction(MultiUserFile.Actions.UserCreation, user.get(UserAtt.RACF));

        String cmd = "powershell.exe " + "\"" +
                StaticHelper.NEW_EMPOLIS_USER_SKRIPT +
                " -Racf " + "'" + user.getString(UserAtt.RACF) + "'" +
                " -Vorname " + "'" + user.getString(UserAtt.Vorname) + "'" +
                " -Nachname " + "'" + user.getString(UserAtt.Nachname) + "'" +
                " -Standort " + "'" + user.getString(UserAtt.Standort) + "'" +
                " -Position " + "'" + user.getString(UserAtt.Position) + "'" +
                " -Abteilung " + "'" + user.getString(UserAtt.Abteilung) + "'" +
                " -Vorgesetzter " + "'" + user.getString(UserAtt.Disziplinarischer_Vorgesetzter) + "'" +
                " -Kostenstelle " + "'" + user.getString(UserAtt.Kostenstelle) + "'" +
                (user.hasAtt(UserAtt.Durchwahl) ? (" -Durchwahl " + user.getString(UserAtt.Durchwahl)) : "") +
                (user.hasAtt(UserAtt.Vertragsende) ? (" -Vertragsende " + user.getString(UserAtt.Vertragsende)) : "") +
                " -Firma " + "'" + user.getString(UserAtt.Firma) + "'"
                + "\"";

        switchToWorkPanel();
        progressBar.updateBar("init", 1, true);
        ShellRunner shellRunner = new ShellRunner(cmd,10,460);
        shellRunner.addErrorHandler(ShellRunner.getStandardErrorLogger());
        shellRunner.addLogHandler(ShellRunner.getStandardStandardLogger());
        shellRunner.addTimerHandler((line, progress) ->
                progressBar.updateBar(line, Math.abs(progress), progress < 0)
        );
        shellRunner.addSpecialHandler(new ConsoleStreamHandler() {
            int azureTimer = 0, licencesTimer = 0;

            @Override
            public void handle(String line, int progress) throws InterruptedException {
                if (line.contains("Sync Azure AD"))
                    azureTimer = 120;
                if (line.contains("License Enabling MFA"))
                    licencesTimer = 180;
                while (azureTimer != 0) {
                    azureTimer--;
                    progressBar.updateBar("Waiting for Azure Sync (" + azureTimer + "/120)", 30 + (120 - azureTimer), false);
                    sleep(1000);
                }
                while (licencesTimer != 0) {
                    licencesTimer--;
                    progressBar.updateBar("Waiting for MS Office(" + licencesTimer + "/180)", 30 + 120 + (180 - licencesTimer), false);
                    sleep(1000);
                }
            }
        });

        shellRunner.addSuccessRunnable(() -> {
            String conout = shellRunner.getStdout();
            String userString = conout.substring(conout.indexOf("NewUserStart") + 13, conout.indexOf("NewUserEnd") - 1);
            logger.info("got userString");
            user.update(content.getUser(user.get(UserAtt.RACF)));
            logger.info("updated gui");
//            Gui.gui.activeView.licences.decreaseByOne(Arrays.asList(License.values()));
            logger.info("decreased licences");
            content.multiUserFile.removeAction(MultiUserFile.Actions.UserCreation, user.get(UserAtt.RACF));
            Dialogs.infoBox("New Empolis user created. You need to sync in SnipeIT and assign licenses:\n" +
                    "- Office 365 E3 / Microsoft 365 E3\n" +
                    "- Sharepoint Server 2019 Enterprise CAL\n" +
                    "- Windows Server 2019 CAL\n" +
                    "- Microsoft Defender für Office 365 (Plan 1)\n" +
                    "- Microsoft Power Automate Free\n\n" + userString, "USER CREATED");
            switchToInputPanel();
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
            logger.info("closed window");
        });
        shellRunner.addFailureRunnable(() -> {
            Dialogs.errorBox("Error adding User.\n" + shellRunner.getErrout(), "Error Adding User");
            switchToInputPanel();
            content.multiUserFile.removeAction(MultiUserFile.Actions.UserCreation, user.get(UserAtt.RACF));
        });


        new Thread(shellRunner).start();

    }

    /**
     * Switches to a view where the inputs are disabled, with a progress bar at the bottom and the output of the console on the right.
     */
    private void switchToWorkPanel() {
        JPanel jPanel = new JPanel();
        progressBar = new ProgressBar(340);
        jPanel.setLayout(new BorderLayout());
        jPanel.add(super.jPanel, BorderLayout.CENTER);
        jPanel.add(progressBar, BorderLayout.SOUTH);
        setContentPane(jPanel);
        setEnabled(false);

        pack();
    }

    /**
     * Switches back to the original view
     */
    private void switchToInputPanel() {
        setContentPane(super.jPanel);

        pack();
        setEnabled(true);
        requestFocus();
    }

}
