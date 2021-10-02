package server.war;

import server.core.AppInit;

import javax.servlet.ServletContext;

public interface AppInitFactory {

    AppInit createInit(ServletContext ctx);
}
