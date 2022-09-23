package content.adContent.content.enums;

/**
 * Eine Enumeration für alle Standorte die für User auswählbar sind. Soll das Dropdown-Menü geändert werden muss nur
 * diese Enum bearbeitet werden
 */
public enum Standort implements StringToEnum {
    Nicht_Gesetzt,
    Berlin,
    Darmstadt,
    Kaiserslautern,
    Bielefeld,
    Rimpar;

    @Override
    public String toString() {
        switch (this) {
            case Nicht_Gesetzt:
                return UserAtt.NICHT_GESETZT;
            default:
                return super.toString();
        }
    }

    public static Standort stringToEnum(String firmaasString) {
        if (firmaasString == null)
            return Nicht_Gesetzt;
        switch (firmaasString) {
            case ("Berlin"):
                return Berlin;
            case ("Darmstadt"):
                return Darmstadt;
            case ("Kaiserslautern"):
                return Kaiserslautern;
            case ("Bielefeld"):
                return Bielefeld;
            case ("Rimpar"):
                return Rimpar;
            default:
                return Nicht_Gesetzt;
        }
    }
}

