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

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

public class Main extends Application {

    String DB_URL = "jdbc:hsqldb:file:" + getFile("HsqlDb Database/hsqldb-database") + ";crypt_key=C3ACDC4DA6A15C33BF2F54804C2EF281;crypt_type=AES;shutdown=true";
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
                Flyway flyway = Flyway.configure().baselineOnMigrate(true)
                        .dataSource(DB_URL, DB_USER, DB_PASSWORD)
                        .locations("db/migration")
                        .sqlMigrationPrefix("V")
                        .load();
                flyway.migrate();

                txtPath.appendText("Database Migrate ------------" + "\n");

                try {
                    Connection con = flyway.getConfiguration().getDataSource().getConnection();

                    ResultSet result = con.createStatement().executeQuery("SELECT * FROM tbl_message");

                    while (result.next()) {
                        txtPath.appendText("Database Message: " + result.getString(1) + " : " + result.getString(2) + "\n" + "------------" + "\n");
                    }
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }


            } catch (FlywayException ex) {
                txtPath.appendText("Database Faild to be migrate : " + ex.getMessage() + " ------------" + "\n");

                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static File getFile(String name) {
        String dir;

        if (Platform.isAndroid()) {
            dir = "Document";
        } else {
            dir = "Documents";
        }

        File path = null;
        try {
            path = StorageService.create()
                    .flatMap(s -> s.getPublicStorage(dir))
                    .orElseThrow(() -> new FileNotFoundException("Could not access: " + dir));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new File(path, name);
    }

}
