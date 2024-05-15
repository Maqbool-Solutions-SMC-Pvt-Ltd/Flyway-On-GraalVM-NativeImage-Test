package com.maqboolsolutions.flywaygraalvmtest;

import java.io.*;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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

                txtPath.appendText(getSQLFiles() + "\n");

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(DB_URL);
                config.setUsername(DB_USER);
                config.setPassword(DB_PASSWORD);
                HikariDataSource dataSource = new HikariDataSource(config);


                String path = Main.class.getClassLoader().getResource("db/migrations").getPath();

                txtPath.appendText("Path: " + path + "\n");

                File file = null;
                try {
                    file = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                String basePath = file.getParent();

                txtPath.appendText("basePath " + basePath + "\n");

                File resourcesDirectory = new File("src/db/migrations");

                Main.class.getClassLoader().getResource("db/migrations").getPath();


                txtPath.appendText("path 1" + resourcesDirectory.getAbsolutePath() + "\n");
                txtPath.appendText("path 1" + Main.class.getResource("/db/migrations").toString() + "\n");
                txtPath.appendText("path 1" + Main.class.getResource("/db/migrations").getPath() + "\n");
                txtPath.appendText("path 1" + Main.class.getResource("/db/migrations").getFile() + "\n");


                Flyway flyway = Flyway.configure()
                        .baselineOnMigrate(true)
                        .dataSource(dataSource)
                        .locations("filesystem:" + path)
                        .sqlMigrationPrefix("V")
                        .load();

                flyway.migrate();

                txtPath.appendText("Database Migrate ------------\n");

                try (Connection con = dataSource.getConnection();
                     ResultSet result = con.createStatement().executeQuery("SELECT * FROM tbl_message")) {
                    while (result.next()) {
                        txtPath.appendText("Database Message: " + result.getString(1) + " : " + result.getString(2) + "\n------------\n");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

            } catch (FlywayException ex) {
                txtPath.appendText("Database Failed to be migrated: " + ex.getMessage() + " ------------\n");
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            } catch (IOException e) {
                throw new RuntimeException(e);
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

    public static String getSQLFiles() throws IOException {
        // Load SQL file from resources
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("db/migrations/V1_0_1__Create_Table.sql");
//        InputStream inputStream = Main.class.getResourceAsStream("/db/migrations/V1_0_1__Create_Table.sql");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read SQL file content
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }

        // Close resources
        reader.close();
        inputStream.close();

        // Use SQL content as needed
        String sqlContent = content.toString();

        return sqlContent;
    }

}
