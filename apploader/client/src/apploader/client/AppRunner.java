package apploader.client;

public interface AppRunner {

    void runGui(byte[] data) throws Exception;

    void runGui(String[] args, Class<? extends IFrame> frameClass);
}
