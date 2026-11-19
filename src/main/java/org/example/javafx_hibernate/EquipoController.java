package org.example.javafx_hibernate;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.models.Equipo;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class EquipoController {
    @FXML
    private TableView<Equipo> tableView;
    @FXML
    private TableColumn<Equipo, Integer> idColumn;
    @FXML
    private TableColumn<Equipo, String> nombreColumn;
    @FXML
    private TableColumn<Equipo, String> categoriaColumn;
    @FXML
    private TableColumn<Equipo, Boolean> disponibleColumn;

    private final ObservableList<Equipo> equipos = FXCollections.observableArrayList();
    private SessionFactory sessionFactory;

    @FXML
    private void initialize() {
        configurarColumnas();
        tableView.setItems(equipos);
        try {
            sessionFactory = crearSessionFactory();
        } catch (RuntimeException e) {
            Platform.runLater(() -> mostrarError(e));
            return;
        }
        configurarCierre();
        cargarEquipos();
    }

    private void configurarColumnas() {
        idColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        nombreColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getNombre()));
        categoriaColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getCategoria()));
        disponibleColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getDisponible()));
    }

    private SessionFactory crearSessionFactory() {
        Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/hibernate-local.properties")) {
            if (input == null) {
                throw new IllegalStateException("Falta src/main/resources/hibernate-local.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer hibernate-local.properties", e);
        }
        properties.forEach((key, value) -> configuration.setProperty((String) key, (String) value));
        return configuration.buildSessionFactory();
    }

    private void configurarCierre() {
        tableView.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }
            newScene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
                if (newWindow != null) {
                    newWindow.setOnHidden(event -> {
                        if (sessionFactory.isOpen()) {
                            sessionFactory.close();
                        }
                    });
                }
            });
        });
    }

    private void cargarEquipos() {
        Task<List<Equipo>> task = new Task<>() {
            @Override
            protected List<Equipo> call() {
                try (Session session = sessionFactory.openSession()) {
                    return session.createQuery("select e from Equipo e order by e.id", Equipo.class).getResultList();
                }
            }
        };
        task.setOnSucceeded(event -> equipos.setAll(task.getValue()));
        task.setOnFailed(event -> mostrarError(task.getException()));
        Thread thread = new Thread(task, "hibernate-loader");
        thread.setDaemon(true);
        thread.start();
    }

    private void mostrarError(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de conexión");
        alert.setHeaderText("No se pudo cargar los datos de la base de datos");
        alert.setContentText(cause.getMessage());
        alert.showAndWait();
    }
}
