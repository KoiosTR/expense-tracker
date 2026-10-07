package com.expense.controller;

import com.expense.App;
import com.expense.dao.UserDao;
import com.expense.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SettingsDialogController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField profilePictureField;
    @FXML private ComboBox<String> colorPaletteComboBox;

    private final UserDao userDao = new UserDao();
    private MainController mainController;

    @FXML
    public void initialize() {
        colorPaletteComboBox.setItems(FXCollections.observableArrayList(
                "Mor (Zealz) - #5944d1",
                "Mavi - #3b82f6",
                "Yeşil - #10b981",
                "Kırmızı - #ef4444",
                "Koyu Gri - #374151"
        ));

        User currentUser = App.authService.getCurrentUser();
        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            profilePictureField.setText(currentUser.getProfilePicture());
            
            String theme = currentUser.getThemeColor();
            if (theme == null) theme = "#5944d1";
            
            for (String item : colorPaletteComboBox.getItems()) {
                if (item.contains(theme)) {
                    colorPaletteComboBox.getSelectionModel().select(item);
                    break;
                }
            }
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void browseImage() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Profil Resmi Seç");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Resim Dosyaları", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) profilePictureField.getScene().getWindow();
        java.io.File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                // Güvenli bir klasöre kopyala (İndirilenler'den silinirse kaybolmasın)
                java.io.File appDir = new java.io.File(System.getProperty("user.home"), ".zealz");
                if (!appDir.exists()) appDir.mkdirs();
                
                String ext = "";
                String name = selectedFile.getName();
                int i = name.lastIndexOf('.');
                if (i > 0) ext = name.substring(i);
                
                java.io.File targetFile = new java.io.File(appDir, "profile_" + System.currentTimeMillis() + ext);
                java.nio.file.Files.copy(selectedFile.toPath(), targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                profilePictureField.setText(targetFile.toURI().toString());
            } catch (Exception e) {
                e.printStackTrace();
                // Kopyalama başarısız olursa orijinal yolu kullan
                profilePictureField.setText(selectedFile.toURI().toString());
            }
        }
    }

    @FXML
    private void save() {
        User user = App.authService.getCurrentUser();
        if (user == null) return;

        user.setFirstName(firstNameField.getText() != null ? firstNameField.getText().trim() : "");
        user.setLastName(lastNameField.getText() != null ? lastNameField.getText().trim() : "");
        user.setProfilePicture(profilePictureField.getText() != null ? profilePictureField.getText().trim() : "");

        String selectedColor = colorPaletteComboBox.getValue();
        if (selectedColor != null && selectedColor.contains("#")) {
            String hex = selectedColor.substring(selectedColor.indexOf("#"));
            user.setThemeColor(hex);
        }

        boolean success = userDao.updateUser(user);
        if (success) {
            mainController.updateUserProfile();
            close();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Ayarlar kaydedilemedi.");
            alert.showAndWait();
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) firstNameField.getScene().getWindow();
        stage.close();
    }
}
