package content.adContent.content.enums;

import gui.Dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

import content.adContent.content.enums.*;

/**
 * <p> Alle Attribute und Konstanten die einem User zugewiesen werden können.
 * Änderungen müssen nur in diesem Enum und den entsprechenden Methoden vorgenommen werden </p>
 * <p> Jede Art von Attribut muss der Enum als Konstante hinzugefügt werden und in {@link UserAtt#getSearchAtt()} muss
 * der Name des Active Directory Attributs eingetragen werden. Außerdem sollte es in der {@link UserAtt#toString()}
 * Methode eingetragen falls es Sonder- oder Leerzeichen enthält. Sollte das Attribut bestimmte Anforderung erfüllen,
 * müssen diese in {@link UserAtt#inputCorrect(UserAtt, String)} spezifiziert werden.</p>
 * <p> Soll ein Attribut beim anlegen eines neuen Users abgefragt werden, muss es in {@link UserAtt#getNewUserAtt()}
 * oder {@link UserAtt#getOptionalNewUserAtt()} hinzugefügt werden. Soll ein Attribut nicht editierbar sein, muss es in
 * {@link UserAtt#getUneditableUserAtt()} hinzugefügt werden</p>
 * <p> Soll ein Attribut als Enumeration hinzugefügt werden muss eine neue Enumeration im Package <i>enums</i> angelegt
 * werden. Hierbei sollten die bestehenden als Vorlage benutzt werden. Weiterhin muss dieses Attribut in die Methoden
 * {@link UserAtt#isEnum()},{@link UserAtt#getEnum()} und {@link UserAtt#getEnumfromString(String)} eingetragen werden.
 * </p>
 * <p>Soll ein Attribut als Boolean hinzugefügt werden muss dieses in den Methoden {@link UserAtt#isBoolean()},
 * {@link UserAtt#getBooleanfromString(String)} und {@link UserAtt#getStringfromBoolean(boolean)} eingetragen werden.
 * dabei wird im Active Directory der Wert in der {@code getStringfromBoolean} Methode gespeichert.</p>
 * <p>Soll ein Attribut als Date hinzugefügt werden muss dieses in der Methode {@link UserAtt#isDate()} eingetragen
 * werden. Falls der Auswahlzeitraum begrenzt werden soll passiert, muss das in der entsprechenden GUI-Klasse gemacht
 * werden.</p>
 */
public enum UserAtt {
    //String Attributes
    RACF,
    Vorname,
    Nachname,
    Name,
    Position,
    Abteilung,
    Disziplinarischer_Vorgesetzter,
    Fachlicher_Vorgesetzter,
    Kostenstelle,
    Durchwahl,
    Email,

    //Date Attributes
    Vertragsende,

    //Enum Attributes
    Standort,
    Firma,

    //Boolean Attributes
    Enabled;

    public final static String NICHT_GESETZT = "Nicht gesetzt";
    public static final String UNBEKANNT = "UNBEKANNT";


    /**
     * Gibt zurück ob der Input den hier spezifizierten Vorgaben entspricht
     *
     * @param userAtt Das Attribut dem der Input entsprechen soll
     * @param data    Der input
     * @return {@code True} wenn der Input den Vorgaben entspricht, sonst {@code False}
     */
    public static boolean inputCorrect(UserAtt userAtt, String data) {
        switch (userAtt) {
            case RACF: //4 Buchstaben dann 3 Zahlen
                if (data.length() != 7)
                    return false;
                return checkTypeHelper((pos, c) -> {
                    if (pos == 0 || pos == 1 || pos == 2 || pos == 3) return Character.isLetter(c);
                    if (pos == 4 || pos == 5 || pos == 6) return Character.isDigit(c);
                    return false;
                }, data);
            case Vorname: //Buchstaben und Leerzeichen (spezialfall das jemand nur leerzeichen eingibt, wird nicht abgefangen)
            case Name:
                return checkTypeHelper((pos, c) -> (Character.isLetter(c) || c == ' '), data);
            case Nachname:
                return checkTypeHelper((pos, c) -> (Character.isLetter(c) || c == ' ' || c == '-'), data);
            case Position:
                return checkTypeHelper((pos, c) -> (Character.isLetter(c) || c == ' ' || c == '\\' || c == '&' || c == '/'), data);
            case Standort:
                return Arrays.stream(content.adContent.content.enums.Standort.values()).filter(standort1 -> standort1 != content.adContent.content.enums.Standort.Nicht_Gesetzt).anyMatch(standort1 -> standort1.toString().equals(data));
            case Abteilung:
                return checkTypeHelper((pos, c) -> (Character.isLetter(c) || Character.isDigit(c) || c == ' ' || c == '\\' || c == '&' || c == '/' || c == '-'), data);
            case Disziplinarischer_Vorgesetzter:
            case Fachlicher_Vorgesetzter: //Zahlen, Buchstaben, Leerzeichen
                return checkTypeHelper((pos, c) -> (Character.isLetter(c) || Character.isDigit(c) || c == ' '), data);
            case Kostenstelle: //Zahlen
                return checkTypeHelper((pos, c) -> Character.isDigit(c), data);
            case Durchwahl: //Zahlen, ( ) / + und \ sind erlaubt
                return checkTypeHelper((pos, c) -> (Character.isDigit(c) || c == '\\' || c == '+' || c == '/' || c == '(' || c == ')'), data);

            case Vertragsende: //Zahlen und Punkte
                return checkTypeHelper((pos, c) -> (Character.isDigit(c) || c == '.'), data);

            case Email: //Ich gehe davon aus das eine E-Mail immer etwas.etwas@etwas.etwas ist
                if (!data.contains("@")) return false;
                String[] mail = data.split("@");
                if (!mail[0].contains(".") || !mail[1].contains(".")) return false;
                String[] mail1 = mail[0].split("\\.");
                String[] mail2 = mail[1].split("\\.", 2);
                return checkTypeHelper((pos, c) -> (Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == '_'), mail1[0]) && checkTypeHelper((pos, c) -> (Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == '_'), mail1[1]) && checkTypeHelper((pos, c) -> (Character.isLetter(c) || Character.isDigit(c)), mail2[0]) && checkTypeHelper((pos, c) -> (Character.isLetter(c) || Character.isDigit(c) || c == '.'), mail2[1]);
            case Firma:
                return Arrays.stream(content.adContent.content.enums.Firma.values()).filter(firma1 -> firma1 != content.adContent.content.enums.Firma.Nicht_Gesetzt).anyMatch(firma1 -> firma1.toString().equals(data));
        }
        return false;
    }

    /**
     * Gibt alle Attribute zurück, die beim Erstellen eines neuen Users mit {@link gui.contentPanel.ADConnection.AddUser} immer angeben werden müssen
     *
     * @return Die Liste
     */
    public static List<UserAtt> getNewUserAtt() {
        return new ArrayList<>(Arrays.asList(
                Vorname, Nachname, Standort, Position, Abteilung, Disziplinarischer_Vorgesetzter, Kostenstelle, Firma
        ));
    }

    /**
     * Gibt alle Attribute zurück, die beim Erstellen eines neuen Users mit {@link gui.AddUser} optional angeben werden können
     *
     * @return Die Liste
     */
    public static List<UserAtt> getOptionalNewUserAtt() {
        return new ArrayList<>(Arrays.asList(
                Fachlicher_Vorgesetzter, Durchwahl, Vertragsende
        ));
    }

    /**
     * Gibt alle nicht editierbaren Attribute zurück für {@link gui.EditUser}
     *
     * @return Die Liste
     */
    public static List<UserAtt> getUneditableUserAtt() {
        return new ArrayList<>(Arrays.asList(
                RACF, Email, Name,

                //TODO funktioniert aktuell nicht richtig
                Durchwahl
        ));
    }

    @Override
    public String toString() {
        //Alles mit specialChars sollte hier eingetragen werden damit es richtig angezeigt werden kann
        switch (this) {
            case Disziplinarischer_Vorgesetzter:
                return "Disziplinarischer Vorgesetzter";
            case Fachlicher_Vorgesetzter:
                return "Fachlicher Vorgesetzter";
            default:
                return super.toString();
        }
    }

    /**
     * Gibt den Wert zurück, in dem das entsprechende Attribut in der Datenbank gespeichert ist
     *
     * @return Der Key in der Datenbank
     */
    public String getSearchAtt() {
        switch (this) {
            case RACF:
                return "name";
            case Vorname:
                return "givenName";
            case Nachname:
                return "sn";
            case Standort:
                return "l";
            case Position:
                return "title";
            case Abteilung:
                return "department";
            case Disziplinarischer_Vorgesetzter:
                return "manager";
            case Fachlicher_Vorgesetzter:
                return "employeeType";
            case Kostenstelle:
                return "employeeid";
            case Durchwahl:
                return "telephoneNumber";
            case Vertragsende:
                return "accountexpires";
            case Email:
                return "mail";
            case Firma:
                return "company";
            case Enabled:
                return "userAccountControl";
            default:
                return "how did we even get here (probably forgot a case)";
        }
    }

    public boolean isEnum() {
        return (this == Standort || this == Firma);
    }

    public boolean isDate() {
        return  (this == Vertragsende);
    }

    public boolean isBoolean() {
        return this == Enabled;
    }

    public String[] getEnum() {
        switch (this) {
            case Standort:
                return Arrays.stream(content.adContent.content.enums.Standort.values()).map(content.adContent.content.enums.Standort::toString).toArray(String[]::new);
            case Firma:
                return Arrays.stream(content.adContent.content.enums.Firma.values()).map(content.adContent.content.enums.Firma::toString).toArray(String[]::new);
        }
        Dialogs.errorBox("Couldn't find the enum " + this + ". Please check the UserAtt enum for wrong Input (getEnum(), isEnum())", "ENUM NOT FOUND");
        return null;
    }

    public StringToEnum getEnumfromString(String string) {
        switch (this) {
            case Standort:
                return content.adContent.content.enums.Standort.stringToEnum(string);
            case Firma:
                return content.adContent.content.enums.Firma.stringToEnum(string);
        }
        return content.adContent.content.enums.Firma.Nicht_Gesetzt;
    }

    /**
     * Gets the AD String for this boolean in the given state
     *
     * @param state the state
     * @return The String
     */
    public String getStringfromBoolean(boolean state){
        switch (this) {
            case Enabled: return state ? "512" : "514";
        }
        Dialogs.errorBox("Couldn't find the boolean Attribute " + this + ". Please check the UserAtt enum for wrong Input (getStringfromBoolean(), isBoolean())", "ENUM NOT FOUND");
        return null;
    }

    /**
     * Gets the Boolean from the ADString
     *
     * @param state the String
     * @return the appropriate boolean
     */
    public Boolean getBooleanfromString(String state){
        switch (this) {
            case Enabled: return state.equals("512") || state.equals("66048");
        }
        Dialogs.errorBox("Couldn't find the boolean Attribute " + this + ". Please check the UserAtt enum for wrong Input (getStringfromBoolean(), isBoolean())", "ENUM NOT FOUND");
        return null;
    }


    //---DONT EDIT---//
    /**
     * Gibt alle Attribute zurück die für eine Abfrage aus dem AD nötig sind
     *
     * @return Die Liste von Attributen
     */
    public static List<String> getAllSearchAtt() {
        List<String> searchAtt = new ArrayList<>();
        for (int i = 0; i < values().length; i++)
            searchAtt.add((values()[i]).getSearchAtt());
        return searchAtt;
    }

    private static boolean checkTypeHelper(BiPredicate<Integer,Character> check, String string) {
        for (int i = 0; i < string.length(); i++)
            if (!check.test(i, string.charAt(i))) return false;
        return true;
    }

}
