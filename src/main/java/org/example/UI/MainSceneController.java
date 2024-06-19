package org.example.UI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.List;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;
import org.docx4j.model.fields.merge.DataFieldName;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Body;
import org.example.BookmarksReplaceWithText;
import org.example.Files;
import org.example.BookmarksAlterWithFormula;
import org.example.database.config.Configuration;
import org.example.database.config.Properties;
import org.example.formula.FormulaCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.example.UI.SceneManager.createNewAlert;

//TODO: добавить очистку документа от формул
public class MainSceneController implements Initializable {
    @Getter
    private static MainSceneController instance;

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
    private ListView<String> bookmarksListView;
    @FXML
    private CheckBox oldStyleCheckBox;
    @FXML
    private TextArea logTextArea;
    @FXML
    private TextField searchTextField;
    //база данных
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
    private ComboBox<String> configsComboBox;
    //стилизация
    @FXML
    private CheckBox cursiveCheckBox;
    @FXML
    private CheckBox baldCheckBox;
    @FXML
    private CheckBox underlinedCheckBox;
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

    /**
     * Вызывается перед показом сцены и заполняет элементы данными
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        instance = this;
        //заполняется combo box с доступными шрифтами и по умолчанию выбирается Times New Roman
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        fontComboBox.getItems().addAll(fontNames);
        fontComboBox.getSelectionModel().select(ArrayUtils.indexOf(fontNames,"Times New Roman"));
        //задается диапазон возможных размеров шрифта и начальное значение 14
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100,14));

        //заполняется combo box с доступными цветами шрифта и выделения и по умолчанию выбирается черный и желтый
        textColors.put("Черный", "#000000");
        textColors.put("Серый","#808080");
        textColors.put("Cеребряный","#C0C0C0");
        textColors.put("Белый","#FFFFFF");
        textColors.put("Фуксия","#FF00FF");
        textColors.put("Фиолетовый","#800080");
        textColors.put("Красный","#FF0000");
        textColors.put("Темно-бордовый","#800000");
        textColors.put("Желтый","#FFFF00");
        textColors.put("Оливковый","#808000");
        textColors.put("Лайм","#00FF00");
        textColors.put("Зеленый","#008000");
        textColors.put("Аква","#00FFFF");
        textColors.put("Бирюзовый","#008080");
        textColors.put("Синий","#0000FF");
        textColors.put("Темно-синий","#000080");


        highlightColors.put("Черный", "#000000");
        highlightColors.put("Синий", "#0000FF");
        highlightColors.put("Голубой", "#00FFFF");
        highlightColors.put("Зеленый", "#008000");
        highlightColors.put("Пурпурный", "#FF00FF");
        highlightColors.put("Красный", "#FF0000");
        highlightColors.put("Желтый", "#FFFF00");
        highlightColors.put("Белый", "#FFFFFF");
        highlightColors.put("Темно-синий", "#00008B");
        highlightColors.put("Темно-голубой", "#008B8B");
        highlightColors.put("Темно-зеленый", "#006400");
        highlightColors.put("Темно-пурпурный", "#8B008B");
        highlightColors.put("Темно-красный", "#8B0000");
        highlightColors.put("Темно-желтый", "#FFD700");
        highlightColors.put("Темно-серый", "#A9A9A9");
        highlightColors.put("Светло-серый", "#D3D3D3");


        List<String> textColorList = new ArrayList<>(textColors.keySet());
        List<String> highlightColorList = new ArrayList<>(highlightColors.keySet());
        colorComboBox.getItems().addAll(textColorList);
        colorComboBox.getSelectionModel().select(textColorList.indexOf("Черный"));
        highlightComboBox.getItems().addAll(highlightColorList);
        highlightComboBox.getSelectionModel().select(highlightColorList.indexOf("Желтый"));

        updateAvailableConfigs();
    }

    /**
     * Выбор документа с закладками, в который необходимо добавить формулы
     */
    @FXML
    public void chooseDocButtonPressed(){
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Microsoft Word files (*.docx)", "*.docx");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(SceneManager.getPrimaryStage());
        if (file != null) {
            String path = file.getAbsolutePath();
            //проверяем формат файла
            if(!path.endsWith(".docx")){
                createNewAlert(Alert.AlertType.INFORMATION,"","","Формат документа должен быть .docx", ButtonType.OK);
                return;
            }
            fileNameLabel.setText(file.getAbsolutePath());
            fileNameLabel.setVisible(true);
            bookmarksListView.getItems().clear();
            wordMLPackage = null;
        }
    }

    /**
     * Загружает выбранный документ и выводит все закладки выбранного документа в виде ListView для последующего выбора
     */
    @FXML
    public void showDocBookmarksButtonPressed(){
        if(!fileNameLabel.getText().isBlank()){
            try {
                //открываем документ
                wordMLPackage = WordprocessingMLPackage.load(new File(fileNameLabel.getText()));
            }catch (Docx4JException ex){
                log.error(Arrays.toString(ex.getStackTrace()));
                createNewAlert(Alert.AlertType.ERROR,"","","Произошла ошибка при считывании документа", ButtonType.OK);
                return;
            }
            //загружаем закладки документа
            List<String> bookmarkNames = Files.getBookmarkNames(wordMLPackage);
            if (bookmarkNames.isEmpty()) {
                createNewAlert(Alert.AlertType.INFORMATION,"","","В документе нет закладок", ButtonType.OK);
                return;
            }

            //задаем возможность поиска закладок по именам в специальном поле
            ObservableList<String> observableBookmarkNames
                    = FXCollections.observableArrayList(bookmarkNames);
            FilteredList<String> filteredBookmarkNames = new FilteredList(observableBookmarkNames, p -> true);

            searchTextField.textProperty().addListener((obs, oldValue, newValue) -> {
                filteredBookmarkNames.setPredicate(name -> name.toLowerCase().contains(newValue.toLowerCase().trim()));
            });


            bookmarksListView.setItems(filteredBookmarkNames);
            //очищает ранее заданные формулы для закладок
            alterMap.clear();
        }else {
            createNewAlert(Alert.AlertType.INFORMATION,"","","Для продолжения работы, необходимо выбрать файл", ButtonType.OK);
        }
    }

    /**
     *  Добавляет в документ формулы к закладкам и сохраняет его
     */
    @FXML
    public void fillDocumentWithFormulasButtonPressed(){
        if(wordMLPackage == null){
            createNewAlert(Alert.AlertType.INFORMATION,"","","Необходимо сначала загрузить документ в пункте 2", ButtonType.OK);
            return;
        }
        //добавление формул в невидимый элемент
        Body body = Files.getDocumentBody(wordMLPackage);
        BookmarksAlterWithFormula.alterBookmarkContent(body.getContent(),alterMap);

        //сохранение документа
        try {
            wordMLPackage.save(new File(fileNameLabel.getText()));
            logTextArea.appendText("Документ сохранен с новыми формулами\n");
        }catch(Docx4JException ex){
            log.error(Arrays.toString(ex.getStackTrace()));
            createNewAlert(Alert.AlertType.ERROR,"","","Произошла ошибка при сохранении документа, возможно он уже где-то открыт", ButtonType.OK);
        }

    }

    /**
     * Заполняет map имя_закладки-формула чтобы потом их вместе внести в документ
     */
    @FXML
    public void addFormulaButtonPressed(){
        if(bookmarksListView.getSelectionModel().getSelectedItems().size() != 1){
            createNewAlert(Alert.AlertType.INFORMATION,"","","Необходимо сначала выбрать закладку, загрузите для этого документ", ButtonType.OK);
            return;
        }

        //так как по умолчанию можно выбрать только 1 элемент, то берем первый
        DataFieldName bookmarkName = new DataFieldName(bookmarksListView.getSelectionModel().getSelectedItems().get(0));
        //если формула для этой закладки уже существует, то заменяется на новую
        alterMap.remove(bookmarkName);

        //задание параметров для формулы
        //база данных
        try {
            Configuration configuration = Configuration.getInstance();
            Properties properties = configuration.getConfig(configsComboBox.getValue());
            calculator.setDatabaseParams(properties.getUrl(), properties.getUsername(), properties.getPassword(),
                    tableField.getText(), columnField.getText(), primaryKeyField.getText(), primaryKeyValueField.getText());
        }catch (IllegalArgumentException ex){
            log.info(Arrays.toString(ex.getStackTrace()));
            createNewAlert(Alert.AlertType.INFORMATION,"","","Поля базы данных не должны быть пустыми", ButtonType.OK);
            return;
        }catch (Exception ex){
            createNewAlert(Alert.AlertType.ERROR,"","","Ошибка при работе с файлом конфигурации", ButtonType.OK);
            log.error(Arrays.toString(ex.getStackTrace()));
        }
        //шрифт
        calculator.setFont(fontComboBox.getValue(),fontSizeSpinner.getValue());
        //стилизация
        if(highlightCheckBox.isSelected())//если нужно выделить текст
            calculator.setStyle(cursiveCheckBox.isSelected(),baldCheckBox.isSelected(),underlinedCheckBox.isSelected(),
                    highlightColors.get(highlightComboBox.getValue()), textColors.get(colorComboBox.getValue()));
        else//если выделение не требуется
            calculator.setStyle(cursiveCheckBox.isSelected(),baldCheckBox.isSelected(),underlinedCheckBox.isSelected(),
                    "absent", textColors.get(colorComboBox.getValue()));
        //сохранять ли старый стиль текста при подстановке
        calculator.setOldStyle(oldStyleCheckBox.isSelected());

        //добавление формулы в map, для последующего отображения в файле
        alterMap.put(bookmarkName, calculator.calculate());
        logTextArea.appendText("К закладке " + bookmarksListView.getSelectionModel().getSelectedItems().get(0) + " добавлена формула " + calculator.calculate() + "\n");
    }

    /**
     * Удаляет подготовленные к внесению документ формулы из списка
     */
    @FXML
    public void clearFormulasButtonPressed(){
        alterMap.clear();
        logTextArea.appendText("Список закладок был очищен\n");
    }

    /**
     * Заполняет документ по ранее записанным формулам
     */
    @FXML
    public void  fillInDocumentWithDataButtonPressed(){
        if(wordMLPackage == null){
            createNewAlert(Alert.AlertType.INFORMATION,"","","Необходимо сначала загрузить документ в пункте 2", ButtonType.OK);
            return;
        }

        try {
            Body body = Files.getDocumentBody(wordMLPackage);
            //замена текста закладки
            BookmarksReplaceWithText.replaceBookmarkContent(body.getContent());
            //сохранение документа
            wordMLPackage.save(new File(fileNameLabel.getText()));
            logTextArea.appendText("Документ был сохранен с новым содержимым\n");
        }catch (SQLException sqlException){
            createNewAlert(Alert.AlertType.ERROR,"","","Произошла ошибка при считывании данных из бд", ButtonType.OK);
            log.error(Arrays.toString(sqlException.getStackTrace()));
        }catch (Docx4JException ex){
            createNewAlert(Alert.AlertType.ERROR,"","","Произошла ошибка при сохранении документа, возможно он уже где-то открыт", ButtonType.OK);
            log.error(Arrays.toString(ex.getStackTrace()));
        }catch (Exception ex){
            createNewAlert(Alert.AlertType.ERROR,"","","В документе содержится закладка с некорректной формулой", ButtonType.OK);
            log.error(Arrays.toString(ex.getStackTrace()));
        }

    }

    /**
     * Делает видимым выбор цвета выделения, если активен соответствующий чек-бокс
     */
    @FXML
    private void highlightCheckBoxPressed(){
        highlightComboBox.setVisible(highlightCheckBox.isSelected());
    }

    @FXML
    private void closeButtonPressed(){
        SceneManager.closePrimaryStage();
    }

    @FXML
    private void addConfigButtonPressed(){
        SceneManager.addSceneOnNewAdditionalStage("addConfig","Добавление конфигурации",500.0,300.0);
    }

    @FXML
    private void deleteConfigButtonPressed(){
        SceneManager.addSceneOnNewAdditionalStage("deleteConfig","Удаление конфигурации",500.0,300.0);
    }

    @FXML
    private void showHelpButtonPressed() throws IOException {
        SceneManager.showHelp();
    }

    /**
     * При выборе конфигурации из списка, отображает в полях информацию о ней
     */
    @FXML
    private void configurationSelected() {
        try {
            Configuration configuration = Configuration.getInstance();
            if(configsComboBox.getValue()!=null) {
                Properties properties = configuration.getConfig(configsComboBox.getValue());
                urlTextField.setText(properties.getUrl());
                usernameTextField.setText(properties.getUsername());
                passwordTextField.setText(properties.getPassword());
            }
        } catch (Exception ex) {
            createNewAlert(Alert.AlertType.ERROR,"","","Ошибка при работе с файлом конфигурации", ButtonType.OK);
            log.error("Ошибка при работе с файлом конфигурации\n" + Arrays.toString(ex.getStackTrace()));
        }
    }

    public void updateAvailableConfigs(){
        try {
            Configuration configuration = Configuration.getInstance();
            configsComboBox.getItems().clear();
            configsComboBox.getItems().addAll(configuration.getConfigNames());
        } catch (Exception ex) {
            createNewAlert(Alert.AlertType.ERROR,"","","Ошибка при работе с файлом конфигурации", ButtonType.OK);
            log.error("Ошибка при работе с файлом конфигурации\n" + Arrays.toString(ex.getStackTrace()));
        }
    }
}
