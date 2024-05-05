package org.example.UI;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
import org.docx4j.wml.Body;
import org.example.BookmarksReplaceWithText;
import org.example.Files;
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

    private static final Map<String,String> textColors = new HashMap<>();
    private static final Map<String,String> highlightColors = new HashMap<>();//выделение не поддерживает всех цветов

    private static WordprocessingMLPackage wordMLPackage = null;

    //настройки
    @FXML
    private Label fileNameLabel;
    @FXML
    private ComboBox<String> bookmarksListComboBox;
    @FXML
    private CheckBox oldStyleCheckBox;
    @FXML
    private TextArea logTextArea;
    //база данных
    @FXML
    private TextField databaseField;
    @FXML
    private TextField tableField;
    @FXML
    private TextField columnField;
    @FXML
    private TextField primaryKeyField;
    @FXML
    private TextField primaryKeyValueField;
    @FXML
    private TextField urlTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;
    @FXML
    private ComboBox<String> databaseTypeComboBox;
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
        textColors.put("Black",	"#000000");
        textColors.put("Gray","#808080");
        textColors.put("Silver","#C0C0C0");
        textColors.put("White","#FFFFFF");
        textColors.put("Fuchsia","#FF00FF");
        textColors.put("Purple","#800080");
        textColors.put("Red","#FF0000");
        textColors.put("Maroon","#800000");
        textColors.put("Yellow","#FFFF00");
        textColors.put("Olive","#808000");
        textColors.put("Lime","#00FF00");
        textColors.put("Green","#008000");
        textColors.put("Aqua","#00FFFF");
        textColors.put("Teal","#008080");
        textColors.put("Blue","#0000FF");
        textColors.put("Navy","#000080");


        highlightColors.put("Black", "#000000");
        highlightColors.put("Blue", "#0000FF");
        highlightColors.put("Cyan", "#00FFFF");
        highlightColors.put("Green", "#008000");
        highlightColors.put("Magenta", "#FF00FF");
        highlightColors.put("Red", "#FF0000");
        highlightColors.put("Yellow", "#FFFF00");
        highlightColors.put("White", "#FFFFFF");
        highlightColors.put("DarkBlue", "#00008B");
        highlightColors.put("DarkCyan", "#008B8B");
        highlightColors.put("DarkGreen", "#006400");
        highlightColors.put("DarkMagenta", "#8B008B");
        highlightColors.put("DarkRed", "#8B0000");
        highlightColors.put("DarkYellow", "#FFD700");
        highlightColors.put("DarkGray", "#A9A9A9");
        highlightColors.put("LightGray", "#D3D3D3");


        List<String> textColorList = new ArrayList<>(textColors.keySet());
        List<String> highlightColorList = new ArrayList<>(highlightColors.keySet());
        colorComboBox.getItems().addAll(textColorList);
        colorComboBox.getSelectionModel().select(textColorList.indexOf("Black"));
        highlightComboBox.getItems().addAll(highlightColorList);
        highlightComboBox.getSelectionModel().select(highlightColorList.indexOf("Yellow"));

        //TODO: заполнение combo box возможными бд
        //Обработка неправильно введенных данных бд, в том числе значения главного ключа(не нашли данные)
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
            if(!path.endsWith(".docx")){
                Alert alert = new Alert(Alert.AlertType.ERROR, "Формат документа должен быть .docx", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            fileNameLabel.setText(file.getAbsolutePath());
            fileNameLabel.setVisible(true);
            bookmarksListComboBox.getItems().clear();
            wordMLPackage = null;
        }
    }

    /**
     *  Выводит все закладки выбранного документа в виде выпадающего списка для последующего выбора
     */
    @FXML
    public void showDocBookmarksButtonPressed() throws Docx4JException {
        if(!fileNameLabel.getText().isBlank()){
            //открываем документ
            wordMLPackage = WordprocessingMLPackage.load(new File(fileNameLabel.getText()));
            //загружаем закладки документа
            List<String> bookmarkNames = Files.getBookmarkNames(wordMLPackage);
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
        if(bookmarksListComboBox.getValue() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Необходимо сначала выбрать закладку, загрузите для этого документ", ButtonType.OK);
            alert.showAndWait();
            return;
        }
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
        //база данных
        calculator.setDatabaseParams(urlTextField.getText(),usernameTextField.getText(),passwordTextField.getText(),databaseTypeComboBox.getValue(),
                databaseField.getText(),tableField.getText(),columnField.getText(),primaryKeyField.getText(),primaryKeyValueField.getText());
        //шрифт
        calculator.setFont(fontComboBox.getValue(),fontSizeSpinner.getValue());
        //стилизация
        if(highlightCheckBox.isSelected())//если нужно выделить текст
            calculator.setStyle(cursiveCheckBox.isSelected(),baldCheckBox.isSelected(),
                    highlightColors.get(highlightComboBox.getValue()), textColors.get(colorComboBox.getValue()));
        else//если выделение не требуется
            calculator.setStyle(cursiveCheckBox.isSelected(),baldCheckBox.isSelected(),
                    "absent", textColors.get(colorComboBox.getValue()));
        //сохранять ли старый стиль текста при подстановке
        calculator.setOldStyle(oldStyleCheckBox.isSelected());


        //добавление формулы в map, для последующего отображения в файле
        alterMap.put(dataFieldName, calculator.calculate());
        logTextArea.appendText("Added formula " + calculator.calculate() + " for bookmark " + bookmarksListComboBox.getValue() + "\n");
        log.info("Added formula " + calculator.calculate() + " for bookmark " + bookmarksListComboBox.getValue());
    }

    /**
     * удаляет подготовленные к внесению документ формулы из списка
     * @throws Exception
     */
    @FXML
    public void clearFormulasButtonPressed() throws Exception {
        alterMap.clear();
        logTextArea.appendText("List of formulas has been cleared\n");
        log.info("List of formulas has been cleared");
    }

    /**
     *  Добавляет в документ формулы к закладкам и сохраняет его
     */
    @FXML
    public void fillDocumentButtonPressed() throws Exception {
        if(wordMLPackage == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Необходимо сначала загрузить документ в пункте 2", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        //добавление формул в невидимый элемент
        Body body = Files.getDocumentBody(wordMLPackage);
        BookmarksAlterWithFormula.alterBookmarkContent(body.getContent(),alterMap);
        log.info("Previously created formulas have been added to the document");
        logTextArea.appendText("Previously created formulas have been added to the document\n");

        //сохранение документа
        //wordMLPackage.save(new File("C:\\Users\\krasi\\Desktop\\Programms\\Java\\Bookmarks\\src\\main\\java\\org\\example\\templates\\RESULT.docx"));
        //wordMLPackage.save(new File("C:\\Users\\krasi\\IdeaProjects\\Bookmarks\\src\\main\\java\\org\\example\\templates\\RESULT.docx"));
        wordMLPackage.save(new File(fileNameLabel.getText()));
        //TODO: Сохранить отдельный файл или изменять существующий
        log.info("Document has been saved");
        logTextArea.appendText("Document has been saved\n");
    }

    /**
     * Подставляет в документ значения в соответствии с заданными в нем формулами
     */
    @FXML
    public void  fillInDocumentButtonPressed() throws Exception {
        if(wordMLPackage == null){
            Alert alert = new Alert(Alert.AlertType.ERROR, "Необходимо сначала загрузить документ в пункте 2", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        //подстановка по формулам
        //заполнение map закладка - текст_подстановки
        Map<DataFieldName, String> replaceMap = new HashMap<DataFieldName, String>();
        replaceMap.put( new DataFieldName("paragraph1"), "parChange1");
        replaceMap.put( new DataFieldName("paragraph2"), "parChange2");
        replaceMap.put( new DataFieldName("DOCX"), "ChangeDOCX1");
        //замена текста закладки
        Body body = Files.getDocumentBody(wordMLPackage);
        BookmarksReplaceWithText.replaceBookmarkContents(body.getContent(), replaceMap);

        //сохранение документа
        //wordMLPackage.save(new File("C:\\Users\\krasi\\Desktop\\Programms\\Java\\Bookmarks\\src\\main\\java\\org\\example\\templates\\RESULT.docx"));
        //wordMLPackage.save(new File("C:\\Users\\krasi\\IdeaProjects\\Bookmarks\\src\\main\\java\\org\\example\\templates\\RESULT.docx"));
        wordMLPackage.save(new File(fileNameLabel.getText()));
        //TODO: Сохранить отдельный файл или изменять существующий
        log.info("Document has been saved");
        logTextArea.appendText("Document has been saved\n");
    }

    /**
     * Делает видимым выбор цвета выделения, если активен соответствующий чек-бокс
     */
    @FXML
    private void highlightCheckBoxPressed() throws Exception {
        highlightComboBox.setVisible(highlightCheckBox.isSelected());
    }
}
