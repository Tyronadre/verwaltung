package content.adContent.content.enums;

/**
 * Eine Enumeration für alle Firmen die für User auswählbar sind. Soll das Dropdown-Menü geändert werden muss nur diese
 * Enum bearbeitet werden
 */
public enum Firma implements StringToEnum{

    Nicht_Gesetzt,
    Empolis_Information_Management_GmbH,
    Empolis_Intelligent_Views;

    @Override
    public String toString() {
        switch (this) {
            case Nicht_Gesetzt:
                return UserAtt.NICHT_GESETZT;
            case Empolis_Intelligent_Views:
                return "Empolis Intelligent Views";
            case Empolis_Information_Management_GmbH:
                return "Empolis Information Management GmbH";
        }
        return UserAtt.UNBEKANNT;
    }


    public static Firma stringToEnum(String firmaasString) {
        if (firmaasString == null)
            return Nicht_Gesetzt;
        switch (firmaasString) {
            case ("i-views GmbH"):
            case ("Empolis Intelligent Views"):
                return Empolis_Intelligent_Views;
            case ("Empolis Information Management GmbH"):
                return Empolis_Information_Management_GmbH;
            default:
                return Nicht_Gesetzt;
        }
    }
}
