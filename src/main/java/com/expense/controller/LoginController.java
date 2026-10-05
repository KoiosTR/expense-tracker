package com.expense.controller;

import com.expense.App;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Hata", "Lütfen kullanıcı adı ve şifre giriniz.");
            return;
        }

        if (App.authService.login(username, password)) {
            try {
                App.setRoot("main");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showAlert("Hata", "Geçersiz kullanıcı adı veya şifre.");
        }
    }

    @FXML
    private void register() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Hata", "Lütfen kullanıcı adı ve şifre giriniz.");
            return;
        }

        if (App.authService.register(username, password)) {
            showAlert("Başarılı", "Kayıt başarılı. Şimdi giriş yapabilirsiniz.");
        } else {
            showAlert("Hata", "Kayıt başarısız. Kullanıcı adı alınmış olabilir.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
