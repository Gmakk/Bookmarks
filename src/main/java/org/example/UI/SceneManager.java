package org.example.UI;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Класс для загрузки и переключения сцен
 * аннотация @FXML нужна для того, чтобы методы можно было использовать из .fxml
 */
public class SceneManager {
    private static final Logger log = Logger.getLogger(SceneManager.class.getName());
    private static Stage primaryStage;
    private static Map<String,Stage> additionalStages = new HashMap<>();

    public static void setPrimaryStage(Stage primaryStage) {
        SceneManager.primaryStage = primaryStage;
    }

    public static Stage getPrimaryStage(){return primaryStage;}


    /**
     * Метод устанавливает нужную сцену и загружает ее, если такой еще не было
     * @param sceneName Имя нужной сцены(должно совпадать с соответствующим .fxml файлом)
     * @param width ширина сцены
     * @param height высота сцены
     */
    private static void setSceneOnStage(String sceneName,Stage stage, Double width, Double height){
        try {
            //загрузка нового FXML файла
            FXMLLoader loader = new FXMLLoader();
            URL xmlUrl = SceneManager.class.getResource("/scenes/" + sceneName + ".fxml");
            loader.setLocation(xmlUrl);
            Parent root = loader.load();
            //создание из него сцены
            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
        }catch (IOException ex){
            log.info(Arrays.toString(ex.getStackTrace()));
        }
    }

    public static void setSceneOnPrimaryStage(String sceneName){
        setSceneOnStage(sceneName,primaryStage, primaryStage.getWidth(), primaryStage.getHeight());
    }

    public static void closeMainStage(){
        closeAdditionalStages();
        primaryStage.close();
    }

    public static void addSceneOnNewAdditionalStage(String name, Double width, Double height){
        if(!additionalStages.containsKey(name)) {
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setTitle(name);
            additionalStages.put(name,newStage);
        }
        setSceneOnStage(name,additionalStages.get(name),width,height);
        additionalStages.get(name).show();
    }

    public static void closeAdditionalStage(String name){
        if(additionalStages.containsKey(name)) {
            additionalStages.get(name).close();
            additionalStages.remove(name);
        }else
            throw new IllegalArgumentException("There is no additional stage with name " + name);
    }

    public static void closeAdditionalStages(){
        for(Stage stage : additionalStages.values()) {
            stage.close();
        }
    }

}