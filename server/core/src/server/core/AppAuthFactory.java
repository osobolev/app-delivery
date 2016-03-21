package server.core;

import sqlg2.db.SessionFactory;

public interface AppAuthFactory {

    SessionFactory getAuthentificator(String application, LoginData login);
}
