package org.example.UI;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для загрузки и переключения сцен
 * аннотация @FXML нужна для того, чтобы методы можно было использовать из .fxml
 */
public class SceneManager {
    private static Map<String,Scene> scenes = new HashMap<>();
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage primaryStage) {
        SceneManager.primaryStage = primaryStage;
    }

    public static Stage getPrimaryStage(){return primaryStage;}

    /**
     * Метод устанавливает нужную сцену и загружает ее, если такой еще не было
     * @param name Имя нужной сцены(должно совпадать с соответствующим .fxml файлом)
     * @throws IOException Выбрасывает при невозможности загрузить сцену
     */
    private static void setScene(String name) throws IOException {
        if(!scenes.containsKey(name)) {
            //загрузка нового FXML файла
            FXMLLoader loader = new FXMLLoader();
            URL xmlUrl = SceneManager.class.getResource("/scenes/" + name + ".fxml");
            loader.setLocation(xmlUrl);
            Parent root = loader.load();
            //создание из него сцены
            Scene scene = new Scene(root,744,403);
            scenes.put(name,scene);
        }
        //устанавливаем отображаемую сцену
        primaryStage.setScene(scenes.get(name));
    }

    @FXML
    public static void setMainScene() {
        try {
            setScene("main");
        }catch (IOException ex){
            System.out.println("Unable to load main scene");
            primaryStage.close();
        }
    }


    @FXML
    public static void closeStage(){
        primaryStage.close();
    }
}
