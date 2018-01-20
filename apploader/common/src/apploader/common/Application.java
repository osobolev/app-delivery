package apploader.common;

public final class Application {

    public final String id;
    public final String name;

    public Application(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
