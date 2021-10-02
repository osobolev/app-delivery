package sample;

import server.core.AppInit;
import server.war.AppInitFactory;

import javax.servlet.ServletContext;

public final class SampleInitFactory implements AppInitFactory {

    @Override
    public AppInit createInit(ServletContext ctx) {
        return new SampleInit(new SampleLogger());
    }
}
