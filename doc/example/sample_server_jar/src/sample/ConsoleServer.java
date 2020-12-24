package sample;

import server.core.LoginData;
import server.jetty.AppLogin;
import server.jetty.AppServerComponent;
import server.jetty.JettyHttpContainer;
import sqlg3.runtime.SqlTrace;

import java.io.File;

public final class ConsoleServer {

    public static void main(String[] args) throws Exception {
        SampleLogger logger = new SampleLogger();
        JettyHttpContainer container = new JettyHttpContainer(logger);
        SampleInit init = new SampleInit(logger);
        AppServerComponent component = new AppServerComponent("sample", "Sample application", init);
        AppLogin login = application -> new LoginData("org.h2.Driver", "jdbc:h2:mem:", null, null);
        component.init(login, logger, SqlTrace.createDefault(logger::error));
        container.addApplication(component);
        int port = 8080;
        File root = new File("root");
        container.init(port, root);
        container.start();
        component.start();
        System.out.printf("Started server on port %d, root %s\n", port, root);
    }
}
