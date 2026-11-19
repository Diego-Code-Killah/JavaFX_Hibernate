module org.example.javafx_hibernate {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.naming;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires mysql.connector.j;
    requires static lombok;

    opens org.example.javafx_hibernate to javafx.fxml;
    opens org.example.models to org.hibernate.orm.core;

    exports org.example.javafx_hibernate;
    exports org.example.models;
}
