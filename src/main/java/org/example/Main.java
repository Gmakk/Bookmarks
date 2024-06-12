package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.example.UI.SceneManager;
import org.example.formula.Formula;
import org.example.formula.FormulaCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;



public class Main extends Application {
    protected static Logger log = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        //отключаем возможность изменять размер окна
        primaryStage.setResizable(false);
        primaryStage.setWidth(1100.0);
        primaryStage.setHeight(650.0);
        //устанавливаем название
        primaryStage.setTitle("Formula calculator");
        //задаем Stage, в который будут устанавливаться нужные сцены
        SceneManager.setPrimaryStage(primaryStage);
        //включаем первую сцену
        SceneManager.setSceneOnPrimaryStage("main");
        primaryStage.show();

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

    public static void main(String[] args) throws Exception {
        Application.launch(args);
    }
}