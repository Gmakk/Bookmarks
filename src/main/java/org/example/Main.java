package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
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
        //устанавливаем название
        primaryStage.setTitle("Formula calculatorSceneManager");
        //задаем Stage, в который будут устанавливаться нужные сцены
        SceneManager.setPrimaryStage(primaryStage);
        //включаем первую сцену
        SceneManager.setMainScene();
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        Application.launch(args);
    }
}