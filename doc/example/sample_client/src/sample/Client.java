package sample;

import apploader.client.AppInfo;
import apploader.client.SimpleApp;
import sample.dao.IClientDB;
import txrpc.remote.client.DefaultHttpClient;
import txrpc.remote.client.HttpConnectionFactory;
import txrpc.remote.client.IHttpClient;
import txrpc.remote.client.SafeDBInterface;
import txrpc.remote.common.IRemoteDBInterface;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;

public final class Client extends SimpleApp {

    @Override
    protected void run(String application, String[] args) throws Exception {
        IHttpClient client = DefaultHttpClient.builder(new URL(AppInfo.httpServerUrl, application + "/remoting")).build();
        HttpConnectionFactory factory = new HttpConnectionFactory(application, client);
        try (IRemoteDBInterface conn = new SafeDBInterface(Throwable::printStackTrace, factory.openConnection("", ""))) {
            IClientDB db = conn.getSimpleTransaction().getInterface(IClientDB.class);
            Timestamp t = db.getTime();
            String message = "Текущее время: " + DateFormat.getDateTimeInstance().format(t);
            if (GraphicsEnvironment.isHeadless()) {
                System.out.println(message);
            } else {
                JOptionPane.showMessageDialog(null, message);
            }
        }
    }
}
