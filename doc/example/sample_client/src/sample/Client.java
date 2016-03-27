package sample;

import sample.dao.IClientDB;

import apploader.client.*;
import sqlg2.db.*;
import sqlg2.db.client.*;

import java.net.Proxy;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import javax.swing.JOptionPane;

public final class Client extends SimpleApp {

    protected void run(String application, String[] args) throws Exception {
        HttpConnectionFactory factory = new HttpConnectionFactory(new URL(AppInfo.httpServerUrl, "remoting"), Proxy.NO_PROXY, application);
        SQLGLogger logger = new SQLGLogger.Simple();
        IRemoteDBInterface conn = new SafeDBInterface(logger, factory.openConnection("postgres", "admin123"));
        IClientDB db = conn.getSimpleTransaction().getInterface(IClientDB.class);
        Timestamp t = db.getTime();
        JOptionPane.showMessageDialog(null, "Текущее время: " + DateFormat.getDateTimeInstance().format(t));
    }
}
