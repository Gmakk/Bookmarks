package org.example.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import lombok.Setter;
import org.example.database.config.Configuration;
import org.example.database.config.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import static org.example.UI.SceneManager.createNewAlert;

public class AddConfigController {
    protected static Logger log = LoggerFactory.getLogger(AddConfigController.class);

    @FXML
    private TextField titleTextField;
    @FXML
    private TextField urlTextField;
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField passwordTextField;

    @FXML
    private void addButtonPressed() {
        try {
            Configuration configuration = Configuration.getInstance();
            configuration.addConfig(titleTextField.getText(), new Properties(urlTextField.getText(), usernameTextField.getText(), passwordTextField.getText()));
            MainSceneController.getInstance().updateAvailableConfigs();
            createNewAlert(Alert.AlertType.INFORMATION,"","","Информация о подключении к бд добавлена", ButtonType.OK);
            //проверяется возможность подключения с такими параметрами
            Properties properties = configuration.getConfig(titleTextField.getText());
            //если подключиться не удастся, то выкинется SQLException
            Connection connection;
            if(properties.getUsername().isEmpty() || properties.getPassword().isEmpty())
                connection = DriverManager.getConnection(properties.getUrl());
            else
                connection = DriverManager.getConnection(properties.getUrl(), properties.getUsername(), properties.getPassword());
            connection.close();
        } catch (SQLException ex) {
            createNewAlert(Alert.AlertType.INFORMATION,"","","Ошибка при попытке подключения к бд с указанными параметрами", ButtonType.OK);
            log.info("Ошибка при попытке подключения к бд с указанными параметрами\n" + Arrays.toString(ex.getStackTrace()));
        } catch (Exception ex) {
            createNewAlert(Alert.AlertType.ERROR,"","","Ошибка при работе с файлом конфигурации", ButtonType.OK);
            log.error("Ошибка при работе с файлом конфигурации\n" + Arrays.toString(ex.getStackTrace()));
        }
    }
}
