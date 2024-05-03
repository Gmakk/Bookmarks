package org.example.UI;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.example.Main;
import org.example.Test;
import org.example.BookmarksAlterWithFormula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: добавить взаимодействие с несколькими типами бд из выпадающего списка
public class MainSceneController {
    protected static Logger log = LoggerFactory.getLogger(MainSceneController.class);

    @FXML
    private Label fileNameLabel;

    @FXML
    private ComboBox<String> bookmarksListComboBox;

    //при нажатии кнопки "добавить формулу к закладке", постепенно заполняется именами закладок
    // и их формулами для последующего добавления в документ
    private static Map<DataFieldName, String> alterMap = new HashMap<DataFieldName, String>();

    /**
     * Выбор документа с закладками, в который необходимо добавить формулы
     */
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

    /**
     *  Выводит все закладки выбранного документа в виде выпадающего списка для последующего выбора
     */
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

    /**
     * Заполняет map имя_закладки-формула чтобы потом их вместе внести в документ
     */
    @FXML
    public void addFormulaButtonPressed() throws Exception {
        //добавление формул
        //TODO: Вычисление формулы
        alterMap.put(new DataFieldName(bookmarksListComboBox.getValue()), "test#7");
        log.info("Added formula " + "test#7" + " for bookmark " + bookmarksListComboBox.getValue());
    }


    @FXML
    public void clearFormulasButtonPressed() throws Exception {
        alterMap.clear();
        log.info("List of formulas has been cleared");
    }

    /**
     *  Добавляет в документ формулы к закладкам и сохраняет его
     */
    @FXML
    public void fillDocumentButtonPressed() throws Exception {
        Body body = Test.getDocumentBody(fileNameLabel.getText());
        BookmarksAlterWithFormula.alterBookmarkContent(body.getContent(),alterMap);
        log.info("Previously created formulas have been added to the document:\n" + alterMap);
        //((MainDocumentPart)((Document)body.getParent()).getParent()).getPackage().save(new File(fileNameLabel.getText()));
        ((MainDocumentPart)((Document)body.getParent()).getParent()).getPackage().save(new File("C:\\Users\\krasi\\Desktop\\Programms\\Java\\Bookmarks\\src\\main\\java\\org\\example\\templates\\RESULT.docx"));
        //TODO: Сохранить
        log.info("Document has been saved");
    }
}
