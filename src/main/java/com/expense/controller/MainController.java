package com.expense.controller;

import com.expense.App;
import com.expense.dao.CategoryDao;
import com.expense.dao.ExpenseDao;
import com.expense.model.Category;
import com.expense.model.Expense;
import com.expense.util.CsvExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Expense> expenseTable;
    @FXML private TableColumn<Expense, LocalDate> dateCol;
    @FXML private TableColumn<Expense, String> categoryCol;
    @FXML private TableColumn<Expense, String> descriptionCol;
    @FXML private TableColumn<Expense, BigDecimal> amountCol;
    @FXML private TableColumn<Expense, String> currencyCol;
    @FXML private TableColumn<Expense, BigDecimal> amountTlCol;

    @FXML private Label totalLabel;

    private final ExpenseDao expenseDao = new ExpenseDao();
    private final CategoryDao categoryDao = new CategoryDao();

    private ObservableList<Expense> expenseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        categoryCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoryName()));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        currencyCol.setCellValueFactory(new PropertyValueFactory<>("currency"));
        amountTlCol.setCellValueFactory(new PropertyValueFactory<>("amountTl"));

        expenseTable.setItems(expenseList);
        loadData();
    }

    public void loadData() {
        int userId = App.authService.getCurrentUser().getId();
        List<Expense> expenses = expenseDao.getExpensesByUserId(userId);
        expenseList.setAll(expenses);

        BigDecimal total = BigDecimal.ZERO;
        for (Expense e : expenses) {
            total = total.add(e.getAmountTl());
        }
        totalLabel.setText("Toplam Harcama (TL): " + total.toString());
    }

    @FXML
    private void openAddExpense() {
        openExpenseDialog(null);
    }

    @FXML
    private void openEditExpense() {
        Expense selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen düzenlemek için bir harcama seçin.");
            return;
        }
        openExpenseDialog(selected);
    }

    @FXML
    private void deleteExpense() {
        Expense selected = expenseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Uyarı", "Lütfen silmek için bir harcama seçin.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Emin misiniz?");
        confirm.setHeaderText("Harcama silinecek");
        confirm.setContentText("Bu harcamayı silmek istediğinize emin misiniz?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = expenseDao.deleteExpense(selected.getId(), App.authService.getCurrentUser().getId());
            if (success) {
                loadData();
            } else {
                showAlert("Hata", "Silme işlemi başarısız.");
            }
        }
    }

    private void openExpenseDialog(Expense expense) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("expense_dialog.fxml"));
            Parent root = loader.load();

            ExpenseDialogController controller = loader.getController();
            controller.setMainController(this);
            if (expense != null) {
                controller.setExpenseForEdit(expense);
            }

            Stage stage = new Stage();
            stage.setTitle(expense == null ? "Yeni Harcama" : "Harcamayı Düzenle");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exportToCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CSV Olarak Kaydet");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Dosyaları", "*.csv"));
        File file = fileChooser.showSaveDialog(expenseTable.getScene().getWindow());

        if (file != null) {
            boolean success = CsvExporter.exportToCsv(expenseList, file.getAbsolutePath());
            if (success) {
                showAlert("Başarılı", "Harcamalar dışa aktarıldı: " + file.getAbsolutePath());
            } else {
                showAlert("Hata", "Dışa aktarma sırasında bir hata oluştu.");
            }
        }
    }

    @FXML
    private void manageCategories() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Kategori Ekle");
        dialog.setHeaderText("Yeni bir kategori ekleyin");
        dialog.setContentText("Kategori Adı:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                boolean success = categoryDao.createCategory(App.authService.getCurrentUser().getId(), name.trim());
                if (!success) {
                    showAlert("Hata", "Kategori eklenemedi. Zaten mevcut olabilir.");
                } else {
                    showAlert("Başarılı", "Kategori eklendi.");
                }
            }
        });
    }

    @FXML
    private void logout() throws IOException {
        App.authService.logout();
        App.setRoot("login");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
