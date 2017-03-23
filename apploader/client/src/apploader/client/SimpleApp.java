package apploader.client;

public abstract class SimpleApp implements AppFactory {

    public AppRunner newApplication(String application) {
        return args -> run(application, args);
    }

    protected abstract void run(String application, String[] args) throws Exception;
}
