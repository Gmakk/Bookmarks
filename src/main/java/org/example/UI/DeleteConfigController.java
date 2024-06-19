package org.example.UI;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import lombok.Setter;
import org.example.database.config.Configuration;
import org.example.database.config.Properties;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.example.UI.SceneManager.createNewAlert;

public class DeleteConfigController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(DeleteConfigController.class);
    private static Configuration configuration;

    @FXML
    private ComboBox<String> configsComboBox;
    @FXML
    private TextField urlTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //сцена заполняется доступными конфигами
        try {
            configuration = Configuration.getInstance();
            configsComboBox.getItems().addAll(configuration.getConfigNames());
            //configsComboBox.getSelectionModel().select(0);
        } catch (Exception ex) {
            createNewAlert(Alert.AlertType.ERROR,"","","Ошибка при работе с файлом конфигурации", ButtonType.OK);
            log.error("Ошибка при работе с файлом конфигурации\n" + Arrays.toString(ex.getStackTrace()));
        }
    }

    /**
     * При выборе конфигурации из списка, отображает в полях информацию о ней
     */
    @FXML
    private void configurationSelected() {
        if (configsComboBox.getValue() != null) {
            Properties properties = configuration.getConfig(configsComboBox.getValue());
            urlTextField.setText(properties.getUrl());
            usernameTextField.setText(properties.getUsername());
            passwordTextField.setText(properties.getPassword());
        }
    }

    @FXML
    private void deleteConfigButtonPressed() {
        try {
            if (configuration.removeConfig(configsComboBox.getValue()) == 1) {
                //на главном экране обновляется информация о конфигурациях
                MainSceneController.getInstance().updateAvailableConfigs();
                configsComboBox.getItems().clear();
                configsComboBox.getItems().addAll(configuration.getConfigNames());
                //тк конфигурация не выбрана, то поля с информацией о ней пустые
                passwordTextField.setText("");
                usernameTextField.setText("");
                urlTextField.setText("");
                createNewAlert(Alert.AlertType.INFORMATION,"","","Информация о подключении к бд удалена", ButtonType.OK);
            }
        } catch (Exception ex) {
            createNewAlert(Alert.AlertType.ERROR,"","","Ошибка при работе с файлом конфигурации", ButtonType.OK);
            log.error("Ошибка при работе с файлом конфигурации\n" + Arrays.toString(ex.getStackTrace()));
        }
    }
}
