package com.maqboolsolutions.flywaygraalvmtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Platform;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

public class Main extends Application {

    String DB_URL = "jdbc:hsqldb:file:" + getFile("sampledb");
    String DB_USER = "SA";
    String DB_PASSWORD = "";
    String DRIVER = "org.hsqldb.jdbc.JDBCDriver";

    @Override
    public void start(Stage pStage) {
        VBox root = new VBox(10);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(10));

        Button btnCreateDb = new Button("Java-based Migrate Database");

        TextArea txtPath = new TextArea();
        VBox.setVgrow(txtPath, Priority.ALWAYS);

        root.getChildren().addAll(btnCreateDb, txtPath);

        Scene scene;
        if (Platform.getCurrent().equals(Platform.ANDROID)) {
            Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
            scene = new Scene(root, visualBounds.getWidth(), visualBounds.getHeight());
        } else {
            scene = new Scene(root);
        }

        pStage.setTitle("Flyway On GraalVM NativeImage Test!");
        pStage.setScene(scene);
        pStage.show();

        btnCreateDb.setOnAction((event) -> {
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(DB_URL);
                config.setUsername(DB_USER);
                config.setPassword(DB_PASSWORD);
                HikariDataSource dataSource = new HikariDataSource(config);

                Flyway flyway = Flyway.configure()
                        .baselineOnMigrate(true)
                        .dataSource(dataSource)
                        .locations("db/migration")
                        .sqlMigrationPrefix("V")
                        .load();
                flyway.migrate();

                txtPath.appendText("Database Migrate ------------\n");

                try (Connection con = dataSource.getConnection();
                     ResultSet result = con.createStatement().executeQuery("SELECT * FROM tbl_message")) {
                    while (result.next()) {
                        txtPath.appendText("Database Message: " + result.getString(1) + " : " + result.getString(2) + "\n------------\n");
                    }
                }

            } catch (FlywayException | SQLException ex) {
                txtPath.appendText("Database Failed to be migrated: " + ex.getMessage() + " ------------\n");
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static File getFile(String name) {
        String dir = Platform.isAndroid() ? "Document" : "Documents";

        File path = null;
        try {
            path = StorageService.create()
                    .flatMap(s -> s.getPublicStorage(dir))
                    .orElseThrow(() -> new FileNotFoundException("Could not access: " + dir));
        } catch (FileNotFoundException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }

        File folder = new File(path, name);

        return new File(folder, name);
    }

}
