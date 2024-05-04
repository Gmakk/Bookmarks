package org.example.UI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.Document;
import org.example.Main;
import org.example.Test;
import org.example.BookmarksAlterWithFormula;
import org.example.formula.FormulaCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: добавить взаимодействие с несколькими типами бд из выпадающего списка
//TODO: добавить очистку документа от формул
public class MainSceneController implements Initializable {
    protected static Logger log = LoggerFactory.getLogger(MainSceneController.class);

    private static final FormulaCalculator calculator = new FormulaCalculator();

    //при нажатии кнопки "добавить формулу к закладке", поочередно заполняется именами закладок
    // и их формулами для последующего добавления в документ
    private static final Map<DataFieldName, String> alterMap = new HashMap<>();

    private static final Map<String,String> colors = new HashMap<>();

    private static WordprocessingMLPackage wordMLPackage;

    //настройки
    @FXML
    private Label fileNameLabel;
    @FXML
    private ComboBox<String> bookmarksListComboBox;
    @FXML
    private CheckBox oldStyleCheckBox;
    //база данных
    @FXML
    private TextField databaseField;
    @FXML
    private TextField tableField;
    @FXML
    private TextField columnField;
    @FXML
    private TextField primaryKeyField;
    //стилизация
    @FXML
    private CheckBox cursiveCheckBox;
    @FXML
    private CheckBox baldCheckBox;
    @FXML
    private CheckBox highlightCheckBox;
    @FXML
    private ComboBox<String> highlightComboBox;
    @FXML
    private ComboBox<String> colorComboBox;
    //шрифт
    @FXML
    private ComboBox<String> fontComboBox;
    @FXML
    private Spinner<Integer> fontSizeSpinner;
    //форматирование
    //подстановка


    /**
     * Вызывается перед показом сцены и заполняет элементы данными
     */
    //заполнение элементов сцены данными перед показом
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //заполняется combo box с доступными шрифтами и по умолчанию выбирается Times New Roman
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        fontComboBox.getItems().addAll(fontNames);
        fontComboBox.getSelectionModel().select(ArrayUtils.indexOf(fontNames,"Times New Roman"));
        //задается диапазон возможных размеров шрифта и начальное значение 14
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100,14));

        //заполняется combo box с доступными цветами шрифта и выделения и по умолчанию выбирается черный и желтый
        colors.put("Black",	"#000000");
        colors.put("Gray","#808080");
        colors.put("Silver","#C0C0C0");
        colors.put("White","#FFFFFF");
        colors.put("Fuchsia","#FF00FF");
        colors.put("Purple","#800080");
        colors.put("Red","#FF0000");
        colors.put("Maroon","#800000");
        colors.put("Yellow","#FFFF00");
        colors.put("Olive","#808000");
        colors.put("Lime","#00FF00");
        colors.put("Green","#008000");
        colors.put("Aqua","#00FFFF");
        colors.put("Teal","#008080");
        colors.put("Blue","#0000FF");
        colors.put("Navy","#000080");
        List<String> colorList = new ArrayList<>(colors.keySet());
        colorComboBox.getItems().addAll(colorList);
        colorComboBox.getSelectionModel().select(colorList.indexOf("Black"));
        highlightComboBox.getItems().addAll(colorList);
        highlightComboBox.getSelectionModel().select(colorList.indexOf("Yellow"));
    }

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
            // Открываем документ
            wordMLPackage = WordprocessingMLPackage.load(new File(fileNameLabel.getText()));

            List<String> bookmarkNames = Test.getBookmarkNames(wordMLPackage);
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
        if(databaseField.getText().isBlank() || tableField.getText().isBlank() ||
                columnField.getText().isBlank() || primaryKeyField.getText().isBlank()){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Поля базы данных не должны быть пустыми", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        DataFieldName dataFieldName = new DataFieldName(bookmarksListComboBox.getValue());
        //если формула для этой закладки уже существует, то заменяется на новую
        alterMap.remove(dataFieldName);

        //задание параметров для формулы
        calculator.setDatabaseParams(databaseField.getText(),tableField.getText(),columnField.getText(),primaryKeyField.getText());
        calculator.setFont(fontComboBox.getValue(),fontSizeSpinner.getValue());
        if(highlightCheckBox.isSelected())//если нужно выделить текст
            calculator.setStyle(cursiveCheckBox.isSelected(),baldCheckBox.isSelected(),
                    highlightComboBox.getValue(), colorComboBox.getValue());
        else//если выделение не требуется
            calculator.setStyle(cursiveCheckBox.isSelected(),baldCheckBox.isSelected(),
                    "false", colorComboBox.valueProperty().getName());
        calculator.setOldStyle(oldStyleCheckBox.isSelected());

        alterMap.put(dataFieldName, calculator.calculate());
        log.info("Added formula " + calculator.calculate() + " for bookmark " + bookmarksListComboBox.getValue());
    }

    /**
     * удаляет подготовленные к внесению документ формулы из списка
     * @throws Exception
     */
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
        Body body = Test.getDocumentBody(wordMLPackage);
        BookmarksAlterWithFormula.alterBookmarkContent(body.getContent(),alterMap);
        log.info("Previously created formulas have been added to the document:\n" + alterMap);
        //wordMLPackage.save(new File("C:\\Users\\krasi\\Desktop\\Programms\\Java\\Bookmarks\\src\\main\\java\\org\\example\\templates\\RESULT.docx"));
        wordMLPackage.save(new File("C:\\Users\\krasi\\IdeaProjects\\Bookmarks\\src\\main\\java\\org\\example\\templates\\RESULT.docx"));
        //TODO: Сохранить отдельный файл или изменять существующий
        log.info("Document has been saved");
    }

    @FXML
    public void highlightCheckBoxPressed() throws Exception {
        highlightComboBox.setVisible(highlightCheckBox.isSelected());
    }
}
