package apploader.lib;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class AppProperties {

    final List<File> jarList = new ArrayList<>();
    final List<File> dllList = new ArrayList<>();
    String mainClass = null;

    public String getMainClass() {
        return mainClass;
    }
}
