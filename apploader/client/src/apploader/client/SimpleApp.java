package apploader.client;

public abstract class SimpleApp implements AppFactory {

    public AppRunner newApplication(final String application) {
        return new AppRunner() {
            public void runGui(String[] args) throws Exception {
                run(application, args);
            }
        };
    }

    protected abstract void run(String application, String[] args) throws Exception;
}
