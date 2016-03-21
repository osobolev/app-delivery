package apploader.client;

public interface AppFactory {

    AppRunner newApplication(String application);
}
