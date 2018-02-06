package apploader.lib;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;

public final class AppClassLoader extends URLClassLoader {

    private final HashMap<String, File> dllMap = new HashMap<>();

    public AppClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public void addJar(URL url) {
        addURL(url);
    }

    public void addDLL(File file) {
        dllMap.put(file.getName().toUpperCase(), file);
    }

    protected String findLibrary(String libname) {
        File file = dllMap.get(libname.toUpperCase());
        if (file == null) {
            file = dllMap.get(System.mapLibraryName(libname).toUpperCase());
        }
        if (file == null)
            return null;
        return file.getAbsolutePath();
    }

    public void update(List<File> jarList, List<File> dllList) throws Exception {
        for (File file : jarList) {
            addJar(file.toURI().toURL());
        }
        for (File file : dllList) {
            addDLL(file);
        }
    }
}
