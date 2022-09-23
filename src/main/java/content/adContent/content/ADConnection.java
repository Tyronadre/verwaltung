package content.adContent.content;


import content.adContent.content.enums.ADServer;
import content.adContent.content.enums.UserAtt;
import gui.Dialogs;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>
 * The Connection to the local active directory from Empolis. The address is static: {@code ldap://empolis.local:389}.
 * </p>
 * <p>
 * The Connection is not secured in any form. Since we can only access via internal network, we do send the name and pw
 * for login as plain text through the network.
 * </p>
 * <p>
 * This class contains all needed functions for user management in the active directory, like searching, editing and
 * deleting users.</p>
 */
public class ADConnection {
    Boolean connected;
    LdapContext ldapContext;
    final Logger logger;

    /**
     * Creates a new {@link ADConnection}. This new connection doesn't have an initial context or connection.
     */
    public ADConnection() {
        connected = false;
        ldapContext = null;
        logger = Logger.getLogger("ADConnection");
//        logger.addHandler(StaticHelper.logToFile());
    }

    /**
     * Used to authenticate a user given a username/password and domain name.
     * Provides an option to identify a specific an Active Directory server.
     *
     * @param username The username as plain String
     * @param password The password as plain String
     * @return The Connection
     * @throws NamingException If there is an error with creating this Connection
     */
    public static LdapContext getConnection(ADServer provider, String username, String password) throws NamingException {
        Hashtable<String, String> ldapEnv = new Hashtable<>(11);
        ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        ldapEnv.put(Context.PROVIDER_URL, provider.getURL());
        ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple");
        ldapEnv.put(Context.SECURITY_PRINCIPAL, "CN=" + username + provider.getDistinguishedName());
        ldapEnv.put(Context.SECURITY_CREDENTIALS, password);
        return new InitialLdapContext(ldapEnv, null);
        //CN=Henrik Bornemann,OU=OU-Employees,OU=OU-Users,DC=ivda,DC=i-views,DC=de
    }

    /**
     * Establishes an unsecure connection to the active directory at the given provider
     *
     * @return The User that connected to the AD
     */
    public String connect(ADServer provider) {
        String[] pwReq = Dialogs.pwBox();
        logger.info("authorize with ad");
        switch (pwReq[2]) {
            case "Cancel":
                logger.info("Sync Canceled");
                return null;
            case "Forgot":
                Dialogs.infoBox("Empty Credentials", "AD Sync Error");
                logger.info("No Input");
                return null;
            default:
                try {
                    ldapContext = getConnection(provider, pwReq[0], pwReq[1]);
                    connected = true;
                } catch (NamingException e) {
                    if (e.getMessage().contains("error code 49")) {
                        Dialogs.infoBox("Wrong Credentials", "AD Sync Error");
                        logger.warning("Wrong Credentials");
//                    } else if (e.getMessage().contains("empolis:local:389")) {
//                        Dialogs.infoBox("Could not connect", "AD Sync Error");
//                        logger.warning("Could not connect to AD. Maybe no internet/Not in internal network?");
                    } else {
                        Dialogs.infoBox(e.getMessage(), "AD Sync Error");
                        logger.warning("Error logging into AD" + e.getMessage());
                    }
                }
        }
        return pwReq[0];
    }

    /**
     * Get the specified user from the active directory. Should only be called if connected.
     *
     * @param racf The RACF of the user to return
     * @return a new User with all attributes set to the active directory entry, or null if no user was found
     */
    public User getUser(String racf) {
        logger.info("Get User" + racf + "from AD");
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        searchCtls.setReturningAttributes(UserAtt.getAllSearchAtt().toArray(new String[0]));
        NamingEnumeration<SearchResult> answer;
        try {
            answer = ldapContext.search("CN=Users,dc=empolis,dc=local", "(&(objectClass=user))", searchCtls);
            while (answer.hasMoreElements()) {
                SearchResult sr = answer.next();
                if (sr.getAttributes().get(UserAtt.RACF.getSearchAtt()).toString().equals(racf))
                    return new User(sr.getAttributes());
            }
        } catch (NamingException e) {
            logger.info("Could not find the user: " + e.getMessage());
            return null;
        }
        logger.info("Could not find the user: " + racf);
        return null;
    }

    /**
     * Gets a list of all users in the active directory
     *
     * @return the List of all users
     */
    public List<User> getUserList() {
        logger.info("Getting a list of all users from AD");
        SearchControls searchCtls = new SearchControls();
        searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String[] returnedAtts = UserAtt.getAllSearchAtt().toArray(new String[0]);
        searchCtls.setReturningAttributes(returnedAtts);
        NamingEnumeration<SearchResult> answer;
        List<User> userList = new ArrayList<>();
        try {
            answer = ldapContext.search("CN=Users,dc=empolis,dc=local", "(&(objectClass=user))", searchCtls);
            while (answer.hasMoreElements()) {
                SearchResult sr = answer.next();
                userList.add(new User(sr.getAttributes()));
            }
        } catch (NamingException e) {
            logger.severe("Failed to get UserList: " + e.getMessage());
        }
        logger.info("Collected " + userList.size() + " users.");
        return userList;
    }

    /**
     * Changes the user in the active directory with the given modifications.
     *
     * @param name              the racf of the user
     * @param modificationItems the modifications as {@link ModificationItem}s
     * @return {@code True} if the user was edited,{@code False} otherwise
     */
    public boolean changeUser(String name, ModificationItem[] modificationItems) {
        String adName = "CN=" + name + ",CN=Users,DC=empolis,DC=local";
        logger.info("Changing User: " + adName + ". Changes: \n");
        for (ModificationItem modificationItem : modificationItems) {
            logger.info(modificationItem.toString());
        }
        try {
            ldapContext.modifyAttributes(adName, modificationItems);
            logger.info("Changed User");
            Dialogs.infoBox("Successfully edited User. It can take a moment for changes to apply", "Successfully edited User");
            return true;
        } catch (NamingException e) {
            logger.severe("Failed to edit User: " + e.getMessage());
            Dialogs.infoBox(e.getMessage(), "Failed to edit User");
            return false;
        }
    }

    /**
     * Deletes one user in the active directory
     *
     * @param name the racf of the user
     * @return {@code True} if the user was deleted,{@code False} otherwise
     */
    public boolean deleteUser(String name) {
        String adName = "CN=" + name + ",CN=Users,DC=empolis,DC=local";

        logger.info("Deleting User: " + adName);
        throw new UnsupportedOperationException("NYI");
/*
        try {
            //TODO Test this somehow?
            ldapContext.destroySubcontext(adName);
            logger.info("Deleted User");
            Dialogs.infoBox("Deleted User", "Deleted User");
            return true;
        } catch (Exception e) {
            logger.severe("Failed to edit User: " + e.getMessage());
            Dialogs.infoBox(e.getMessage(), "Failed to delete User");
            return false;
        }
 */

    }

    /**
     * Status of this connection
     *
     * @return {@code True} if this ADConnection has an active connection, {@code False} otherwise
     */
    public boolean isConnected() {
        logger.config("connected to ad");
        return connected;
    }

    public void disconnect() {
        connected = false;
    }
}
