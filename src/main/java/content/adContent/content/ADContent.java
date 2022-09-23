package content.adContent.content;

import content.adContent.content.enums.ADServer;
import content.adContent.content.enums.UserAtt;
import gui.Dialogs;
import util.MultiUserFile;
import util.StaticHelper;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ADContent {
    public long lastUpdate = 0L;
    //    public MultiUserFile multiUserFile;
    public final ADConnection adConnection;

    java.util.List<User> contentList;
    java.util.List<String> racfList;
    final Logger logger;
    private String adminUser;
    private final ADServer provider;
    public MultiUserFile multiUserFile;

    public ADContent(ADServer provider) {
        logger = Logger.getLogger("ContentLogger");
        logger.addHandler(StaticHelper.logToFile());
        contentList = new ArrayList<>();
        racfList = new ArrayList<>();
        adConnection = new ADConnection();
        this.provider = provider;

//        updateContent();
    }

    /**
     * @return Die Titel für die Tabelle
     */
    public String[] getTitle() {
        return new String[]{"RACF", "NAME"};
    }

    /**
     * Holt aus dem aktuell gespeicherten Content eine 2D Array in dem je RACF und Name gespeichert sind.
     *
     * @return eine 2D String-Array mit RACF und Name
     */
    public String[][] getContent() {
        //check if available
        if (contentList.isEmpty()) {
            logger.warning("Trying to grab an empty content, probably not synced with AD!");
//            return new String[100][2];
            return new String[][]{{"NO CONTENT", "LOG IN AND SYNC WITH AD!"}};
        }
        //get content otherwise
        String[][] content = new String[contentList.size()][2];
        for (int i = 0; i < contentList.size(); i++) {
            content[i][0] = contentList.get(i).getString(UserAtt.RACF);
            content[i][1] = contentList.get(i).getString(UserAtt.Name);
        }
        return content;
    }

    public void setAdminUser(String user) {
        adminUser = user;
    }

    /**
     * Update diesen Content (Synchronisierung mit AD)
     *
     * @return {@code True} wenn gesynced wurde, sonst {@code false}
     */
    public boolean updateContent() {
        logger.info("Updating Content");
        //Load internal RACFFile
        racfList = RACFFile.getList();

        //Noch nicht verbunden
//        if (!adConnection.isConnected()) {
//            adminUser = adConnection.connect(provider);
////            multiUserFile = new MultiUserFile(adminUser);
//        }
        //Verbunden
        if (adConnection.isConnected()) {
            lastUpdate = System.currentTimeMillis();
            contentList = new ArrayList<>();
            for (User user : adConnection.getUserList()) {
                //Update User List
                contentList.add(user);
                //Update internal RACF from AD
                if (!racfList.contains(user.getString(UserAtt.RACF)) && UserAtt.inputCorrect(UserAtt.RACF, user.getString(UserAtt.RACF))) {
                    RACFFile.add(user.getString(UserAtt.RACF));
                }
            }
            contentList.sort(User::compareTo);
        } else {
            logger.warning("No connection to AD!");
        }
        return adConnection.isConnected();
    }

    /**
     * @return {@code True} wenn ADConnection besteht, sonst {@code false}
     */
    public boolean hasAD() {
        return adConnection.isConnected();
    }
    //---MODIFY USER---//


    /**
     * Editiert einen User
     *
     * @param user              der User der verändert wird
     * @param modificationItems ein Array an {@link ModificationItem}s mit den Veränderungen
     * @return {@code True} wenn der User verändert wurde, sonst {@code False}
     */
    public boolean changeUser(User user, ModificationItem[] modificationItems) {
        return adConnection.changeUser(user.getString(UserAtt.RACF), modificationItems);
    }

    /**
     * Disables a User
     *
     * @param user The User to disable
     */
    public void disableUser(User user) {
        ModificationItem modificationItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(UserAtt.Enabled.getSearchAtt(), UserAtt.Enabled.getStringfromBoolean(false)));

        if (!adConnection.changeUser(user.getString(UserAtt.RACF), new ModificationItem[]{modificationItem}))
            Dialogs.errorBox("Could not disable " + user.get(UserAtt.Name), "ERROR DISABLING USER");
    }

    /**
     * Enables a User
     *
     * @param user The User to enable
     */
    public void enableUser(User user) {
        ModificationItem modificationItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(UserAtt.Enabled.getSearchAtt(), UserAtt.Enabled.getStringfromBoolean(true)));

        if (!adConnection.changeUser(user.getString(UserAtt.RACF), new ModificationItem[]{modificationItem}))
            Dialogs.errorBox("Could not enable " + user.get(UserAtt.Name), "ERROR ENABLING USER");
    }

    //---SEARCH UTILS---//

    /**
     * Searches this Content for Users containing the specified information. This Method is case-insensitive.
     *
     * @param userAtt The Attribute to search for
     * @param data    The String of the Attribute
     * @return A list of users that contains this attribute.
     */
    public java.util.List<User> searchUserList(UserAtt userAtt, String data) {
        java.util.List<User> userList = new ArrayList<>();
        for (User user : contentList) {
            String att = user.get(userAtt);
            if (att != null && Pattern.compile(Pattern.quote(data), Pattern.CASE_INSENSITIVE).matcher(att).find())
                userList.add(user);
        }
        return userList;
    }

    /**
     * Searches this Content for a User with the given RACF.
     *
     * @param racf The RACF to search for
     * @return The User or {@code null}
     */
    public User getUser(String racf) {
        return contentList.stream().filter(user -> user.get(UserAtt.RACF).equals(racf)).findFirst().orElse(null);
    }

    /**
     * Returns if this content has a User with the   or the {@link UserAtt#RACF} given in {@code att}
     *
     * @param racf The RACF
     * @return {@code True} if the user is present, {@code false} otherwise
     */
    public boolean hasUser(String racf) {
        return contentList.stream().anyMatch(user -> user.get(UserAtt.RACF).equals(racf));
    }

    /**
     * Searches for a RACF for the given full name in this Content. This is case-insensitive.
     *
     * @param att The full name (as in {@link UserAtt#Name})
     * @return The RACF or an empty String if none is found
     * @throws IllegalStateException If there are multiple Users with the same name
     */
    public String getRacf(String att) throws IllegalStateException {
        if (racfList.contains(att)) return att;
        List<User> users = new ArrayList<>();
        for (User user : contentList) {
            if (Pattern.compile(Pattern.quote(user.getString(UserAtt.Name)), Pattern.CASE_INSENSITIVE).matcher(att).find())
                users.add(user);
        }
        if (users.size() == 1) return users.get(0).get(UserAtt.RACF);
        else if (users.size() > 1) throw new IllegalStateException("More than one user with this name");
        else return "";
    }

    /**
     * Gets the username of the person that is logged in
     *
     * @return The RACF
     */
    public String getAdmin() {
        return adminUser;
    }

    public ADServer getSelectedServer() {
        return provider;
    }
}
