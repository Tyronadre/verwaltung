package content.adContent.content;

import content.adContent.content.enums.UserAtt;
import gui.Dialogs;
import util.TimeTransformations;

import javax.naming.directory.Attributes;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class User {
    final HashMap<UserAtt, String> userData = new HashMap<>();
    public boolean gettingEdited = false;

    //TODO: telephon vs durchwahl

    public User() {
    }

    /**
     * Baue User aus AD-Attributen auf
     *
     * @param atts Die AD-Attribute
     */
    public User(Attributes atts) {
        for (UserAtt att : UserAtt.values()) {
            if (atts.get(att.getSearchAtt()) == null)
                userData.put(att, null);
            else {
                String attributeAsString = atts.get(att.getSearchAtt()).toString().substring(att.getSearchAtt().length() + 2);
                if (att == UserAtt.Disziplinarischer_Vorgesetzter)
                    userData.put(att, attributeAsString.split(",")[0].substring(3));
                else if (att.isDate()) {
                    userData.put(att, TimeTransformations.dateToString(new Date(TimeTransformations.filetimeToMillis(Long.parseLong(attributeAsString)))));
                } else if (att.isEnum()) {
                    String data = att.getEnumfromString(attributeAsString).toString();
                    if (data == null) {
                        Dialogs.errorBox("Couldnt find the attribute " + attributeAsString + " in the Enum " + att + ". This could lead to problems, please change the entry in AD or the enum.", "Error finding attribute in Enum");
                        userData.put(att, attributeAsString);
                    } else
                        userData.put(att, data);
                } else {
                    userData.put(att, attributeAsString);
                }
            }
        }
        if (hasAtt(UserAtt.Vorname) && hasAtt(UserAtt.Nachname))
            userData.put(UserAtt.Name, getString(UserAtt.Vorname) + " " + getString(UserAtt.Nachname));
    }

    /**
     * Setzt ein Attribut, wenn dies nicht den Vorgaben entspricht wird eine {@link IllegalArgumentException} geworfen
     *
     * @param att  Der Typ des Attributs
     * @param data Die Daten die in dem Attribut gespeichert werden sollen
     */
    public void set(UserAtt att, String data) {
        if (data == null || data.equals(UserAtt.NICHT_GESETZT))
            userData.put(att, null);
        else if (UserAtt.inputCorrect(att, data))
            userData.put(att, data);
        else
            throw new IllegalArgumentException("The given String \"" + data + "\" is not compling to the requirements of " + att);
    }

    public String get(UserAtt userAtt) {
        return userData.get(userAtt);
    }

    /**
     * Gibt das Attribut als String zurück. Unterscheidet sich zu get nur für leere Einträge und booleans.
     * Für Wahrheitswerte wird true oder false zurückgegeben, und nicht der String der im AD gespeichert wird.
     *
     * @param userAtt das gesuchte Attribut
     * @return das Attribut als String, außer wenn das Attribut null ist, dann {@link UserAtt#NICHT_GESETZT}
     */
    public String getString(UserAtt userAtt) {
        if (hasAtt(userAtt))
            if (userAtt.isBoolean())
                return (Objects.requireNonNull(userAtt.getBooleanfromString(userData.get(userAtt)))).toString();
            else return userData.get(userAtt);
        else return UserAtt.NICHT_GESETZT;
    }

    public boolean hasAtt(UserAtt userAtt) {
        return (userData.get(userAtt)) != null;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (UserAtt att : UserAtt.values()) {
            stringBuilder.append("\n").append(att).append("\t");
            switch (att) {
                case Disziplinarischer_Vorgesetzter:
                    break;
                case Fachlicher_Vorgesetzter:
                    stringBuilder.append("\t");
                    break;
                case Nachname:
                case Position:
                case Abteilung:
                case Kostenstelle:
                case Durchwahl:
                case Vertragsende:
                case Standort:
                    stringBuilder.append("\t\t");
                    break;
                default:
                    stringBuilder.append("\t\t\t");
                    break;
            }
            stringBuilder.append(getString(att));
        }
        return stringBuilder.toString();
    }

    public int compareTo(Object o) {
        if (!(o instanceof User))
            throw new IllegalArgumentException();
        return getString(UserAtt.RACF).compareTo(((User) o).getString(UserAtt.RACF));
    }

    /**
     * Updates this user with the Values in the newUser
     *
     * @param newUser The Updated Version of this User
     */
    public void update(User newUser) {
        for (UserAtt userAtt : UserAtt.values()) {
            set(userAtt, newUser.get(userAtt));
        }
    }


}
