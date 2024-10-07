package sample;

import server.core.LoginData;
import server.embedded.AppLogin;
import server.embedded.AppServerComponent;
import server.embedded.EmbeddedHttpContainer;
import server.http.DefaultRequestFactory;
import server.jetty.JettyEmbeddedServer;

import java.io.File;

public final class ConsoleServer {

    public static void main(String[] args) throws Exception {
        SampleLogger logger = new SampleLogger();
        EmbeddedHttpContainer container = new EmbeddedHttpContainer(logger, new JettyEmbeddedServer());
        SampleInit init = new SampleInit(logger);
        AppServerComponent component = new AppServerComponent("sample", "Sample application", new DefaultRequestFactory(), init);
        AppLogin login = application -> new LoginData("org.h2.Driver", "jdbc:h2:mem:", null, null);
        component.init(login, logger);
        container.addApplication(component);
        int port = 8080;
        File root = new File("root");
        container.init(port, "/", root);
        container.start();
        component.start();
        System.out.printf("Started server on port %d, root %s\n", port, root);
    }
}
