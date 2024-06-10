package org.example.UI;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.example.database.config.Configuration;
import org.example.database.config.Properties;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class DeleteConfigController implements Initializable {
    Logger log = Logger.getLogger(DeleteConfigController.class.getName());

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
            Configuration configuration = Configuration.getInstance();
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
        try {
            Configuration configuration = Configuration.getInstance();
            if(configsComboBox.getValue()!=null) {
                Properties properties = configuration.getConfig(configsComboBox.getValue());
                urlTextField.setText(properties.getUrl());
                usernameTextField.setText(properties.getUsername());
                passwordTextField.setText(properties.getPassword());
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ошибка при работе с файлом конфигурации", ButtonType.OK);
            alert.showAndWait();
            log.info("Ошибка при работе с файлом конфигурации\n" + Arrays.toString(ex.getStackTrace()));
        }
    }

    @FXML
    private void deleteConfigButtonPressed() {
        try {
            Configuration configuration = Configuration.getInstance();
            if(configuration.removeConfig(configsComboBox.getValue()) == 1){
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Информация о подключении к бд удалена", ButtonType.OK);
                alert.showAndWait();
            }
            configsComboBox.getItems().clear();
            configsComboBox.getItems().addAll(configuration.getConfigNames());
            passwordTextField.setText("");
            usernameTextField.setText("");
            urlTextField.setText("");
            //configsComboBox.getSelectionModel().select(0);

        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Ошибка при работе с файлом конфигурации", ButtonType.OK);
            alert.showAndWait();
            log.info("Ошибка при работе с файлом конфигурации\n" + Arrays.toString(ex.getStackTrace()));
        }
    }
}
