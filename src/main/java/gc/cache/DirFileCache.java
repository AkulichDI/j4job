package gc.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirFileCache extends AbstractCache<String, String> {

    private final String cachingDir;

    Logger logger = LogManager.getLogger();

    public DirFileCache(String cachingDir) {
        this.cachingDir = cachingDir;
    }


    @Override
    protected String load(String key) {

        try{

        return    Files.readString(Path.of(cachingDir + key));


        } catch (IOException e) {
           logger.error("Ошибка в поиске файла " + e);
        }


        return  null;
    }





}
