package server.jetty;

import server.core.LoginData;

public interface AppLogin {

    LoginData login(String application) throws ServerInitException, UserCancelException;
}
