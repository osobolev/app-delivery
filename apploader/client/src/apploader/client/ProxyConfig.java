package apploader.client;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.Proxy;

public final class ProxyConfig {

    public static final ProxyConfig NO_PROXY = new ProxyConfig(Proxy.NO_PROXY, null, null);

    public final Proxy proxy;
    public final String login;
    public final String password;

    public ProxyConfig(Proxy proxy, String login, String password) {
        this.proxy = proxy;
        this.login = login;
        this.password = password;
    }

    public void setLogin() {
        Authenticator a;
        if (login == null || login.trim().isEmpty()) {
            a = null;
        } else {
            a = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(login, password != null ? password.toCharArray() : new char[0]);
                }
            };
        }
        Authenticator.setDefault(a);
    }
}
