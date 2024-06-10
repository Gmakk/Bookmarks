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
import java.util.logging.Logger;

public class DeleteConfigController implements Initializable {
    private static Logger log = Logger.getLogger(DeleteConfigController.class.getName());
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
        try {
            configuration = Configuration.getInstance();
            configsComboBox.getItems().addAll(configuration.getConfigNames());
            //configsComboBox.getSelectionModel().select(0);
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ошибка при работе с файлом конфигурации", ButtonType.OK);
            alert.showAndWait();
            log.info("Ошибка при работе с файлом конфигурации\n" + Arrays.toString(ex.getStackTrace()));
        }
    }

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
                passwordTextField.setText("");
                usernameTextField.setText("");
                urlTextField.setText("");
                //configsComboBox.getSelectionModel().select(0);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Информация о подключении к бд удалена", ButtonType.OK);
                alert.showAndWait();
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ошибка при работе с файлом конфигурации", ButtonType.OK);
            alert.showAndWait();
            log.info("Ошибка при работе с файлом конфигурации\n" + Arrays.toString(ex.getStackTrace()));
        }
    }
}
