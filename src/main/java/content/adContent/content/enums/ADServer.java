package content.adContent.content.enums;

public enum ADServer {
    EIM("Empolis", ",CN=Users,DC=empolis,DC=local", "ldap://empolis.local"),
    IVIEWS("I-Views",",OU=OU-Users,DC=ivda,DC=i-views,DC=de", "ldap://ivda.i-views.de");
    //CN=Henrik Bornemann,OU=OU-Employees,OU=OU-Users,DC=ivda,DC=i-views,DC=de
    //"ldap://empolis.local", "ldap://ivda.i-views.de"

    final String name;
    final String distinguishedName;
    final String url;
    ADServer(String name, String distinguishedName, String URL) {
        this.name = name;
        this.distinguishedName = distinguishedName;
        this.url = URL;
    }

    public static ADServer getDefaultServer() {
        return EIM;
    }

    public String getURL() {
        return url;
    }

    public String getName() {
        return name;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }
}
