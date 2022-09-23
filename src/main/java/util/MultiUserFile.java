package util;

import gui.Dialogs;

import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A Class with an {@code Actions} enum. For each action will be a unique File created. Supports actions to add and remove
 * an Action that is performed, also to check if an action is performed right now. For this to work the data with which
 * the methods are called needs to be the same.
 */
public class MultiUserFile {
    private static String racf;


    /**
     * The Actions that may be preformed and saved.
     */
    public enum Actions {
        /**
         * Creation of a User
         */
        UserCreation,
        /**
         * Edit of a User
         */
        UserEdit;

        /**
         *
         * @return the file path where this action should be saved
         */
        private String getFilePath() {
            switch (this) {
                case UserEdit:
                    return userEditFile;
                case UserCreation:
                    return userCreationFile;
                default:
                    throw new UnsupportedOperationException("This shouldn't happen!");
            }
        }

        private static String userCreationFile;
        private static String userEditFile;

        static {
            try {
                userCreationFile = (StaticHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/../resources/UserCreation.txt").substring(1);
                userEditFile = (StaticHelper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath() + "/../resources/UserEdit.txt").substring(1);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    public MultiUserFile(String racf) {
        this.racf = racf;
        for (Actions action : Actions.values()) {
            File file = new File(action.getFilePath());
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void setRacf(String racf) {
        MultiUserFile.racf = racf;
    }

    /**
     * Writes the Performed Action to a File. This Method does not check if the written Action is already in the List.
     * {@link MultiUserFile#checkAction} should always be called first.
     *
     * @param action The Performed Action
     * @param data   The Data that is uniquely identifies the performed Action
     */
    public static void saveAction(Actions action, String data) {
        final Set<OpenOption> optionSet = new HashSet<>();
        optionSet.add(StandardOpenOption.WRITE);
        optionSet.add(StandardOpenOption.APPEND);
        ByteBuffer byteBuffer = ByteBuffer.wrap((racf + "__" + data + "\n").getBytes(StandardCharsets.UTF_8));
        Runnable runnable = () -> {
            try (FileChannel channel = FileChannel.open(Paths.get(action.getFilePath()), optionSet)) {
//                while (!channel.isOpen())
//                    wait(5);
                channel.lock();
                channel.write(byteBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        EventQueue.invokeLater(runnable);
    }

    /**
     * Removes an action from the File. This Method does nothing if the action is not the List.
     *
     * @param action The action performed
     * @param data   The Data that is uniquely identifies the performed Action
     * @throws UnsupportedOperationException If an action is removed that was not created by this user.
     */
    public static void removeAction(Actions action, String data) {
        List<String> performedActions = new ArrayList<>();
        boolean foundAction = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(action.getFilePath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] temp = line.split("__");
                if (!temp[1].equals(data))
                    performedActions.add(line);
                else if (!temp[0].equals(racf))
                    throw new UnsupportedOperationException("This should never happen. Can't delete an action started by another user.");
                else
                    foundAction = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!foundAction)
            return;

        File f = new File(action.getFilePath());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            f.delete();
            f.createNewFile();
            if (performedActions.size() > 0) {
                for (String performedAction : performedActions)
                    writer.write(performedAction + "\n");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks if an action with this data is already in the processing.
     * This Method will show an Error Dialog.
     *
     * @param action The action performed
     * @param data   The Data that is uniquely identifies the performed Action
     * @return {@code TRUE} if the action is found, false otherwise.
     */
    public static boolean checkAction(Actions action, String data) {
        try (BufferedReader reader = new BufferedReader(new FileReader(action.getFilePath()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] t = line.split("__");
                if (data.equals(t[1])) {
                    Dialogs.errorBox("Diese Aktion wird aktuell schon von " + t[0] + " ausgeführt.", "Action is currently performed");
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if there is already an action from the given User
     * @param admin the User to check
     * @return {@code TRUE} if there is an action, otherwise {@code FALSE}
     */
    public static boolean hasActionFromUser(String admin) {
        for (Actions action : Actions.values()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(action.getFilePath()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] t = line.split("__");
                    if (admin.equals(t[0])) {
                        return true;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
