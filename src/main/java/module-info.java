module com.expense {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires jbcrypt;
    requires com.google.gson;

    opens com.expense.controller to javafx.fxml;
    opens com.expense.model to javafx.base; // for TableView PropertyValueFactory
    exports com.expense;
}
