package apploader.lib;

public final class ClientProfile {

    public static final ClientProfile DEFAULT = new ClientProfile("", "");

    public final String id;
    public final String name;

    public ClientProfile(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getDisplay() {
        if (name.isEmpty()) {
            if (id.isEmpty()) {
                return "<по умолчанию>";
            } else {
                return id;
            }
        } else {
            return name;
        }
    }

    public String getId() {
        return id.isEmpty() ? "<default>" : id;
    }

    public String toString() {
        return getDisplay();
    }
}
