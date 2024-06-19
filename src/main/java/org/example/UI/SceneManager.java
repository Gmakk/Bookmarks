package org.example.UI;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Класс для загрузки и переключения сцен
 */
public class SceneManager {
    private static final Logger log = Logger.getLogger(SceneManager.class.getName());
    @Getter
    @Setter
    private static Stage primaryStage;
    private static final Map<String,Stage> additionalStages = new HashMap<>();

    /**
     * Метод устанавливает нужную сцену на stage и загружает ее, если такой еще не было
     * @param sceneName Имя нужной сцены(должно совпадать с соответствующим .fxml файлом)
     * @param stage куда устанавливать сцену
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

    public static void closePrimaryStage(){
        closeAdditionalStages();
        primaryStage.close();
    }

    /**
     * Метод для открытия дополнительных окон
     * @param stageName имя stage для поиска среди существующих(совпадает с именем scene fxml файла)
     * @param sceneName отображается как название нового окна
     * @param width ширина сцены
     * @param height высота сцены
     */
    public static void addSceneOnNewAdditionalStage(String stageName,String sceneName, Double width, Double height){
        if(!additionalStages.containsKey(stageName)) {
            Stage newStage = new Stage();
            newStage.setResizable(false);
            newStage.setTitle(sceneName);
            additionalStages.put(stageName,newStage);
        }
        setSceneOnStage(stageName,additionalStages.get(stageName),width,height);
        additionalStages.get(stageName).show();
    }

    public static void closeAdditionalStage(String name){
        if(additionalStages.containsKey(name)) {
            additionalStages.get(name).close();
            additionalStages.remove(name);
        }else
            throw new IllegalArgumentException("There is no additional stage with name " + name);
    }

    /**
     * Закрывает все окна кроме основного и уведомлений
     */
    public static void closeAdditionalStages(){
        for(Stage stage : additionalStages.values()) {
            stage.close();
        }
    }

    /**
     * Показывает пользователю сообщение в новом окне
     * @param type тип сообщения(ошибка, информация и тд...)
     * @param title название окна
     * @param headerText текст в заголовке окна
     * @param contentText текст самого сообщения
     * @param buttons набор кнопок
     */
    public static void createNewAlert(Alert.AlertType type, String title, String headerText, String contentText, ButtonType... buttons){
        Alert alert = new Alert(type, contentText, buttons);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(SceneManager.class.getResource("/styles/alert.css").toExternalForm());
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(SceneManager.class.getResource("/pictures/logo32.png").toString()));
        alert.showAndWait();
    }

    /**
     * Открывает справку по приложению в новом окне
     */
    public static void showHelp() throws IOException {
        //загружается сцена, на которой будет располагаться справка
        FXMLLoader loader = new FXMLLoader();
        URL xmlUrl = SceneManager.class.getResource("/scenes/help.fxml");
        loader.setLocation(xmlUrl);
        Parent root = loader.load();
        //параметры отображения
        WebView browser = new WebView();
        Scene helpScene = new Scene(root);
        ((Pane) helpScene.getRoot()).getChildren().add(browser);
        Stage helpStage = new Stage();
        helpStage.setMinHeight(400);
        helpStage.setMinWidth(600);
        //helpStage.setResizable(false);
        helpStage.setTitle("Справка");
        helpStage.setScene(helpScene);
        //загружается файл справки для отображения(docx документ конвертирован в html через сторонне приложение)
        URL url = SceneManager.class.getResource("/help.html");
        browser.getEngine().load(url.toExternalForm());
        helpStage.show();
    }
}