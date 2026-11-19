module org.example.javafx_hibernate {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.javafx_hibernate to javafx.fxml;
    exports org.example.javafx_hibernate;
}