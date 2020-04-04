package sample;

import apploader.client.AppInfo;
import apploader.client.SimpleApp;
import sample.dao.IClientDB;
import sqlg3.remote.client.HttpConnectionFactory;
import sqlg3.remote.client.SafeDBInterface;
import sqlg3.remote.common.IRemoteDBInterface;
import sqlg3.remote.common.SQLGLogger;

import javax.swing.*;
import java.net.Proxy;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;

public final class Client extends SimpleApp {

    protected void run(String application, String[] args) throws Exception {
        HttpConnectionFactory factory = new HttpConnectionFactory(new URL(AppInfo.httpServerUrl, "remoting"), Proxy.NO_PROXY, application);
        IRemoteDBInterface conn = new SafeDBInterface(Throwable::printStackTrace, factory.openConnection("postgres", "admin123"));
        IClientDB db = conn.getSimpleTransaction().getInterface(IClientDB.class);
        Timestamp t = db.getTime();
        JOptionPane.showMessageDialog(null, "Текущее время: " + DateFormat.getDateTimeInstance().format(t));
    }
}
