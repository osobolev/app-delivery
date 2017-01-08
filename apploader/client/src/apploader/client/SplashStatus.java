package apploader.client;

import java.awt.*;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class SplashStatus {

    public static void setStatus(String status) {
        if (status.length() > 0) {
            System.out.println(status);
        }
        SplashScreen splash = SplashScreen.getSplashScreen();
        if (splash == null)
            return;
        Graphics2D g = splash.createGraphics();
        if (g == null)
            return;
        g.setFont(new Font("Dialog", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int height = fm.getHeight();
        int y0 = splash.getSize().height - 10 - height;
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, y0, splash.getSize().width, height);
        g.setPaintMode();
        g.setColor(Color.black);
        int y1 = y0 + fm.getAscent();
        g.drawString(status, 10, y1);
        splash.update();
    }

    public static void error(Throwable ex) {
        ex.printStackTrace(System.err);
    }
}
