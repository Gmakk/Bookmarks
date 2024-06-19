package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.example.UI.SceneManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;


public class Main extends Application {
    protected static Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage){
        //отключаем возможность изменять размер окна
        primaryStage.setResizable(false);
        primaryStage.setWidth(1100.0);
        primaryStage.setHeight(650.0);
        //устанавливаем название
        primaryStage.setTitle("Калькулятор формул");
        //устанавливаем логотип
        InputStream iconStream = getClass().getResourceAsStream("/pictures/logo32.png");
        Image image = new Image(iconStream);
        primaryStage.getIcons().add(image);
        //задаем Stage, в который будут устанавливаться нужные сцены
        SceneManager.setPrimaryStage(primaryStage);
        //включаем первую сцену
        SceneManager.setSceneOnPrimaryStage("main");
        primaryStage.show();

        //При закрытии основного окна, закрывает все остальные
        primaryStage.setOnHiding(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        SceneManager.closeAdditionalStages();
                        System.exit(0);
                    }
                });
            }
        });
    }

    public static void main(String[] args){
        Application.launch(args);
    }
}