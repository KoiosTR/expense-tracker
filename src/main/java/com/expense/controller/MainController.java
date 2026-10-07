package com.expense.controller;

import com.expense.App;
import com.expense.dao.CategoryDao;
import com.expense.dao.ExpenseDao;
import com.expense.model.Category;
import com.expense.model.Expense;
import com.expense.model.User;
import com.expense.dao.UserDao;
import com.expense.util.CsvExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
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
    @FXML private Label profileNameLabel;
    @FXML private javafx.scene.shape.Circle profileCircle;
    @FXML private Label welcomeLabel;
    @FXML private HBox rootLayout;
    @FXML private AreaChart<String, Number> expenseChart;
    @FXML private FlowPane categoryFlowPane;
    
    @FXML private VBox dashboardPage;
    @FXML private VBox transactionsPage;
    @FXML private VBox walletPage;
    @FXML private Button btnDashboard;
    @FXML private Button btnTransactions;
    @FXML private Button btnWallet;
    @FXML private VBox recentPaymentsVBox;
    
    @FXML private Label lblMonthlyIncome;
    @FXML private Label lblMonthlyBudget;
    @FXML private Label lblSpent;
    @FXML private javafx.scene.layout.Region progressSpent;
    @FXML private javafx.scene.layout.Region progressRemaining;
    
    @FXML private TextField incomeField;
    @FXML private TextField budgetField;

    private final ExpenseDao expenseDao = new ExpenseDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final UserDao userDao = new UserDao();

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
        updateUserProfile();
    }

    public void updateUserProfile() {
        com.expense.model.User user = App.authService.getCurrentUser();
        if (user != null) {
            profileNameLabel.setText(user.getFullName());
            if (welcomeLabel != null) {
                welcomeLabel.setText("Hoş Geldiniz, " + user.getFirstName() + "!");
            }
            String color = user.getThemeColor();
            if (color != null) {
                rootLayout.setStyle("-color-primary: " + color + ";");
            }
            
            String picUrl = user.getProfilePicture();
            if (picUrl != null && !picUrl.trim().isEmpty()) {
                try {
                    javafx.scene.image.Image img = new javafx.scene.image.Image(picUrl, false);
                    if (img.isError()) {
                        System.err.println("Görsel yüklenemedi: " + img.getException());
                    } else if (profileCircle != null) {
                        profileCircle.setFill(new javafx.scene.paint.ImagePattern(img));
                    }
                } catch (Exception e) {
                    System.err.println("Görsel atanırken hata: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("settings_dialog.fxml"));
            Parent root = loader.load();

            SettingsDialogController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Ayarlar");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        int userId = App.authService.getCurrentUser().getId();
        // Refresh currentUser to get updated wallet data
        User updatedUser = userDao.getUserByUsername(App.authService.getCurrentUser().getUsername());
        if (updatedUser != null) {
            App.authService.setCurrentUser(updatedUser);
        } else {
            updatedUser = App.authService.getCurrentUser();
        }
        
        if (welcomeLabel != null) {
            welcomeLabel.setText("Hoş Geldiniz, " + updatedUser.getFirstName() + "!");
        }

        List<Expense> expenses = expenseDao.getExpensesByUserId(userId);
        expenseList.setAll(expenses);

        BigDecimal totalSpent = BigDecimal.ZERO;
        BigDecimal totalIncomeLog = BigDecimal.ZERO;

        for (Expense e : expenses) {
            boolean isIncome = "Maaş".equalsIgnoreCase(e.getCategoryName()) || "Gelir".equalsIgnoreCase(e.getCategoryName());
            if (isIncome) {
                totalIncomeLog = totalIncomeLog.add(e.getAmountTl());
            } else {
                totalSpent = totalSpent.add(e.getAmountTl());
            }
        }
        
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
        
        if (lblMonthlyIncome != null) {
            BigDecimal income = updatedUser.getMonthlyIncome();
            BigDecimal budget = updatedUser.getMonthlyBudget();
            if (income == null) income = BigDecimal.ZERO;
            if (budget == null) budget = BigDecimal.ZERO;

            BigDecimal totalAvailable = income.add(totalIncomeLog).subtract(totalSpent);
            String prefix = totalAvailable.compareTo(BigDecimal.ZERO) < 0 ? "- ₺ " : "₺ ";
            totalLabel.setText(prefix + df.format(totalAvailable.abs()));

            lblMonthlyIncome.setText("₺ " + df.format(income));
            lblMonthlyBudget.setText("₺ " + df.format(budget));
            lblSpent.setText("₺ " + df.format(totalSpent));
            
            // Calculate progress bar widths (Total width is 360)
            double maxProgressWidth = 360.0;
            double spentPercent = 0.0;
            if (budget.compareTo(BigDecimal.ZERO) > 0) {
                spentPercent = totalSpent.doubleValue() / budget.doubleValue();
            }
            spentPercent = Math.min(1.0, Math.max(0.0, spentPercent));
            double spentWidth = maxProgressWidth * spentPercent;
            double remainingWidth = maxProgressWidth - spentWidth;
            
            progressSpent.setPrefWidth(spentWidth);
            progressRemaining.setPrefWidth(remainingWidth);
        }

        // Populate Chart
        if (expenseChart != null) {
            expenseChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            
            java.util.Map<LocalDate, BigDecimal> dailyTotals = new java.util.TreeMap<>();
            for (Expense e : expenses) {
                dailyTotals.merge(e.getDate(), e.getAmountTl(), BigDecimal::add);
            }
            for (java.util.Map.Entry<LocalDate, BigDecimal> entry : dailyTotals.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
            }
            expenseChart.getData().add(series);
        }

        // Populate Categories
        if (categoryFlowPane != null) {
            categoryFlowPane.getChildren().clear();
            java.util.Map<String, BigDecimal> catTotals = new java.util.HashMap<>();
            for (Expense e : expenses) {
                catTotals.merge(e.getCategoryName(), e.getAmountTl(), BigDecimal::add);
            }
            for (java.util.Map.Entry<String, BigDecimal> entry : catTotals.entrySet()) {
                VBox card = new VBox();
                card.getStyleClass().add("category-summary-card");
                Label nameLbl = new Label(entry.getKey());
                nameLbl.getStyleClass().add("category-summary-name");
                Label amtLbl = new Label("₺ " + df.format(entry.getValue()));
                amtLbl.getStyleClass().add("category-summary-amount");
                card.getChildren().addAll(nameLbl, amtLbl);
                categoryFlowPane.getChildren().add(card);
            }
        }
        
        // Populate Recent Payments VBox
        if (recentPaymentsVBox != null) {
            recentPaymentsVBox.getChildren().clear();
            int count = Math.min(expenses.size(), 5);
            for (int i = 0; i < count; i++) {
                Expense e = expenses.get(i);
                
                HBox row = new HBox();
                row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                row.setSpacing(15);
                
                // Dot
                javafx.scene.shape.Circle dot = new javafx.scene.shape.Circle(4);
                // Fake condition: if category is Income, green, else red
                boolean isIncome = "Maaş".equalsIgnoreCase(e.getCategoryName()) || "Gelir".equalsIgnoreCase(e.getCategoryName());
                dot.setFill(javafx.scene.paint.Color.web(isIncome ? "#10b981" : "#ef4444"));
                
                // Titles
                VBox textVBox = new VBox(2);
                Label titleLabel = new Label(e.getCategoryName());
                titleLabel.getStyleClass().add("payment-item-title");
                Label dateLabel = new Label(e.getDate().toString() + " - " + e.getDescription());
                dateLabel.getStyleClass().add("payment-item-date");
                textVBox.getChildren().addAll(titleLabel, dateLabel);
                
                // Spacer
                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                
                // Amount
                String prefix = isIncome ? "+ ₺ " : "- ₺ ";
                Label amountLabel = new Label(prefix + df.format(e.getAmountTl()));
                amountLabel.getStyleClass().add("payment-item-amount");
                amountLabel.getStyleClass().add(isIncome ? "payment-item-amount-pos" : "payment-item-amount-neg");
                
                // Arrow
                Label arrowLabel = new Label(" >");
                arrowLabel.setStyle("-fx-text-fill: #d1d5db; -fx-font-weight: bold;");
                
                row.getChildren().addAll(dot, textVBox, spacer, amountLabel, arrowLabel);
                recentPaymentsVBox.getChildren().add(row);
            }
        }
    }

    @FXML
    private void showDashboard() {
        dashboardPage.setVisible(true);
        dashboardPage.setManaged(true);
        transactionsPage.setVisible(false);
        transactionsPage.setManaged(false);
        if (walletPage != null) {
            walletPage.setVisible(false);
            walletPage.setManaged(false);
        }
        
        btnDashboard.getStyleClass().add("active-menu-button");
        btnTransactions.getStyleClass().remove("active-menu-button");
        btnWallet.getStyleClass().remove("active-menu-button");
    }

    @FXML
    private void showTransactions() {
        dashboardPage.setVisible(false);
        dashboardPage.setManaged(false);
        transactionsPage.setVisible(true);
        transactionsPage.setManaged(true);
        if (walletPage != null) {
            walletPage.setVisible(false);
            walletPage.setManaged(false);
        }
        
        btnDashboard.getStyleClass().remove("active-menu-button");
        btnTransactions.getStyleClass().add("active-menu-button");
        btnWallet.getStyleClass().remove("active-menu-button");
    }
    
    @FXML
    private void showWallet() {
        dashboardPage.setVisible(false);
        dashboardPage.setManaged(false);
        transactionsPage.setVisible(false);
        transactionsPage.setManaged(false);
        if (walletPage != null) {
            walletPage.setVisible(true);
            walletPage.setManaged(true);
            
            User currentUser = App.authService.getCurrentUser();
            if (currentUser != null) {
                incomeField.setText(currentUser.getMonthlyIncome().toString());
                budgetField.setText(currentUser.getMonthlyBudget().toString());
            }
        }
        
        btnDashboard.getStyleClass().remove("active-menu-button");
        btnTransactions.getStyleClass().remove("active-menu-button");
        btnWallet.getStyleClass().add("active-menu-button");
    }

    @FXML
    private void saveWalletSettings() {
        User currentUser = App.authService.getCurrentUser();
        if (currentUser == null) return;
        try {
            BigDecimal income = new BigDecimal(incomeField.getText().trim());
            BigDecimal budget = new BigDecimal(budgetField.getText().trim());
            if (userDao.updateUserWallet(currentUser.getId(), income, budget)) {
                showAlert("Başarılı", "Cüzdan ayarlarınız kaydedildi!");
                loadData(); // refresh dashboard metrics
            } else {
                showAlert("Hata", "Ayarlar kaydedilirken bir sorun oluştu.");
            }
        } catch (NumberFormatException e) {
            showAlert("Hata", "Lütfen geçerli bir sayı giriniz.");
        }
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
        confirm.getDialogPane().getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
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
        dialog.getDialogPane().getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
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
        alert.getDialogPane().getStylesheets().add(App.class.getResource("styles.css").toExternalForm());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
