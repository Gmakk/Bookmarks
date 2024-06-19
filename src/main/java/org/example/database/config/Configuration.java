package org.example.database.config;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    private final HashMap<String, Properties> configs = new HashMap<>();//таблица название-свойства_подключения

    private static final File configFile = new File(System.getProperty("user.dir") +
            System.getProperty("file.separator") + "app.config");

    private static Configuration instance = null;



    private Configuration() throws Exception {
        load();
    }

    public static Configuration getInstance() throws Exception {
        if (instance == null)
            instance = new Configuration();
        return instance;
    }

    /**
     * Загружает из фала конфигурации для подключения к бд
     * @throws Exception Ошибка при чтении файла конфигурации
     */
    private void load() throws Exception{
        HashMap Temp;
        try {
            if(!configFile.exists()){
                configFile.createNewFile();
            }else {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(configFile));
                Temp = (HashMap) ois.readObject();
                if (Temp != null && !Temp.isEmpty())
                    configs.putAll(Temp);
            }
        }catch (EOFException ex) {
            log.info("Файл конфигурации пустой");
        }
    }

    /**
     * Загружает в файл конфигурации для подключения к бд
     * @throws IOException Ошибка при записи файла конфигурации
     */
    private void save() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(configFile));
        oos.writeObject(configs);
        oos.close();
    }

    public void addConfig(String title, Properties properties) throws IOException {
        configs.put(title, properties);
        save();
    }

    public Integer removeConfig(String title) throws IOException {
        if(configs.containsKey(title)) {
            configs.remove(title);
            save();
            return 1;
        }
        return 0;
    }

    public Properties getConfig(String title) throws IllegalArgumentException {
        if (!configs.containsKey(title))
            throw new IllegalArgumentException("No such config: " + title);
        return configs.get(title);
    }

    public List<String> getConfigNames() {
        return new ArrayList<>(configs.keySet());
    }
}
