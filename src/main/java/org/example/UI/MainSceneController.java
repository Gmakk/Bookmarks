package org.example.UI;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.example.Test;

public class MainSceneController {
    @FXML
    private Label fileNameLabel;

    @FXML
    private ComboBox<String> bookmarksListComboBox;

    @FXML
    public void chooseDocButtonPressed(){
        FileChooser fil_chooser = new FileChooser();
        File file = fil_chooser.showOpenDialog(SceneManager.getPrimaryStage());
        String path = file.getAbsolutePath();
        if (file != null) {
            //проверяем формат файла
            if(!path.substring(path.length() - 5).equals(".docx")){
                Alert alert = new Alert(Alert.AlertType.ERROR, "Формат документа должен быть .docx", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            fileNameLabel.setText(file.getAbsolutePath());
            fileNameLabel.setVisible(true);
            bookmarksListComboBox.getItems().clear();
        }
    }


    @FXML
    public void showDocBookmarksButtonPressed() throws Docx4JException {
        if(!fileNameLabel.getText().isBlank()){
            List<String> bookmarkNames = Test.getBookmarkNames(fileNameLabel.getText());
            if(bookmarkNames.isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR, "В документе нет закладок", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            bookmarksListComboBox.getItems().clear();
            bookmarksListComboBox.getItems().addAll(bookmarkNames);
            bookmarksListComboBox.setValue(bookmarkNames.get(0));
        }else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Для продолжения работы, необходимо выбрать файл", ButtonType.OK);
            alert.showAndWait();
        }
    }
}
