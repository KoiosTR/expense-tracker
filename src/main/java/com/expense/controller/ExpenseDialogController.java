package com.expense.controller;

import com.expense.App;
import com.expense.dao.CategoryDao;
import com.expense.dao.ExpenseDao;
import com.expense.model.Category;
import com.expense.model.Expense;
import com.expense.service.CurrencyConverterService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ExpenseDialogController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Category> categoryComboBox;
    @FXML private TextField descriptionField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> currencyComboBox;

    private final CategoryDao categoryDao = new CategoryDao();
    private final ExpenseDao expenseDao = new ExpenseDao();
    private final CurrencyConverterService currencyConverter = new CurrencyConverterService();

    private MainController mainController;
    private Expense editExpense;

    @FXML
    public void initialize() {
        currencyComboBox.setItems(FXCollections.observableArrayList("TRY", "USD", "EUR"));
        currencyComboBox.getSelectionModel().selectFirst();
        datePicker.setValue(LocalDate.now());

        loadCategories();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setExpenseForEdit(Expense expense) {
        this.editExpense = expense;
        datePicker.setValue(expense.getDate());
        descriptionField.setText(expense.getDescription());
        amountField.setText(expense.getAmount().toString());
        currencyComboBox.getSelectionModel().select(expense.getCurrency());

        for (Category cat : categoryComboBox.getItems()) {
            if (cat.getId() == expense.getCategoryId()) {
                categoryComboBox.getSelectionModel().select(cat);
                break;
            }
        }
    }

    private void loadCategories() {
        int userId = App.authService.getCurrentUser().getId();
        List<Category> categories = categoryDao.getCategoriesByUserId(userId);
        categoryComboBox.setItems(FXCollections.observableArrayList(categories));
    }

    @FXML
    private void save() {
        if (datePicker.getValue() == null || categoryComboBox.getValue() == null ||
            descriptionField.getText().trim().isEmpty() || amountField.getText().trim().isEmpty()) {
            showAlert("Hata", "Lütfen tüm alanları doldurunuz.");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountField.getText().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showAlert("Hata", "Tutar sıfırdan büyük olmalıdır.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Hata", "Geçersiz tutar formatı.");
            return;
        }

        String currency = currencyComboBox.getValue();
        BigDecimal amountTl = currencyConverter.convertToTRY(amount, currency);

        if (amountTl == null) {
            showAlert("Hata", "Kur çevrimi başarısız oldu. Lütfen internet bağlantınızı kontrol edin.");
            return;
        }

        Expense expense = editExpense != null ? editExpense : new Expense();
        expense.setUserId(App.authService.getCurrentUser().getId());
        expense.setCategoryId(categoryComboBox.getValue().getId());
        expense.setDate(datePicker.getValue());
        expense.setDescription(descriptionField.getText().trim());
        expense.setAmount(amount);
        expense.setCurrency(currency);
        expense.setAmountTl(amountTl);

        boolean success;
        if (editExpense == null) {
            success = expenseDao.createExpense(expense);
        } else {
            success = expenseDao.updateExpense(expense);
        }

        if (success) {
            mainController.loadData();
            close();
        } else {
            showAlert("Hata", "Harcama kaydedilemedi.");
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) descriptionField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
