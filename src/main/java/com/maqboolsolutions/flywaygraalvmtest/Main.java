package com.maqboolsolutions.flywaygraalvmtest;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gluonhq.charm.down.HelloGluonPlugin;
import com.gluonhq.charm.down.Platform;
import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.FlywayContextService;
import com.gluonhq.charm.down.plugins.StorageService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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

import javafx.util.Pair;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.lsposed.hiddenapibypass.HiddenApiBypass;


public class Main extends Application {

    String DB_URL = "jdbc:hsqldb:file:" + getFile("sampledb");
    String DB_USER = "SA";
    String DB_PASSWORD = "";
    String DRIVER = "org.hsqldb.jdbc.JDBCDriver";
    Path tempDir;
    String customDirectory;
    File[] filesToSave;

    static {
        if (Platform.isAndroid()) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }
    }

    @Override
    public void start(Stage pStage) {
        VBox root = new VBox(10);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(10));

        // Check if the directory already exists
        Path directoryPathFile = Paths.get("directory_path.txt");
        if (Files.exists(directoryPathFile)) {
            try {
                List<String> lines = Files.readAllLines(directoryPathFile);
                if (!lines.isEmpty()) {
                    String directoryPath = lines.get(0); // Read the first line
                    System.out.println("Directory already exists: " + directoryPath);
                    deleteDirectory(directoryPath);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (Platform.isAndroid()) {
            // Create a temporary directory
            File tempDir = FileUtils.createTempDirectory("MyCustomFolder");
            if (tempDir != null) {
                customDirectory = tempDir.getAbsolutePath();
                System.out.println("Custom directory is: " + customDirectory);

                // Save directory path to a file
                saveDirectoryPath(customDirectory);
            } else {
                System.out.println("Failed to create temporary directory");
            }
        } else {
            // Create New Directory.
            try {
                tempDir = Files.createTempDirectory("MyCustomFolder");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            customDirectory = tempDir.toString();
            System.out.println("custom directory is:" + customDirectory);
            // Save directory path to a file
            saveDirectoryPath(customDirectory);
        }

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

        saveFiles();

        btnCreateDb.setOnAction((event) -> {

            try {
                if (Platform.isAndroid()) {
                    Services.get(FlywayContextService.class).ifPresent(FlywayContextService::setContext);
                }

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(DB_URL);
                config.setUsername(DB_USER);
                config.setPassword(DB_PASSWORD);
                HikariDataSource dataSource = new HikariDataSource(config);

//                String path = Main.class.getClassLoader().getResource("db/migration").getPath();
                Flyway flyway = Flyway.configure().baselineOnMigrate(true).dataSource(dataSource)
//                        .locations("db/migration1")
                        .locations("filesystem:" + customDirectory).sqlMigrationPrefix("V").load();
                flyway.migrate();

                txtPath.appendText("Database Migrate ------------\n");

                try (Connection con = dataSource.getConnection(); ResultSet result = con.createStatement().executeQuery("SELECT * FROM tbl_message")) {
                    while (result.next()) {
                        txtPath.appendText("Database Message: " + result.getString(1) + " : " + result.getString(2) + "\n------------\n");
                    }
                }

                deleteFiles();

            } catch (FlywayException | SQLException ex) {
                txtPath.appendText("Database Failed to be migrated: " + ex.getMessage() + " ------------\n");
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private static void saveDirectoryPath(String directoryPath) {
        String filePath = "directory_path.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(directoryPath);
            System.out.println("Directory path saved to: " + filePath);
        } catch (IOException e) {
            // Handle exception
            e.printStackTrace();
        }
    }

    private static void deleteDirectory(String directoryPath) {
        String filePath = "directory_path.txt";
        try {
            Files.walk(Paths.get(filePath)).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Directory deleted: " + directoryPath);
    }

    private void saveFiles() {
        // Connect to Flyway database to retrieve applied migrations
        List<String> appliedMigrations = getAppliedMigrationsFromDatabase();

        // Create directory if it doesn't exist
        File directory = new File(customDirectory);
        if (!directory.exists()) {
            directory.mkdirs(); // Create directories including parent directories
        }

        // Logic to save files
        try {
            URL resourceUrl = null;

            if (Platform.isAndroid()) {
                Optional<List<Pair<InputStream, String>>> migrationListOptional = Services.get(HelloGluonPlugin.class).flatMap(helloGluonPlugin -> {
                    // Call the migrationList method with the path
                    List<Pair<InputStream, String>> list = helloGluonPlugin.migrationList("db/migration");
                    return Optional.ofNullable(list);
                });

                // Extract the migration list if it exists
                List<Pair<InputStream, String>> migrationList = migrationListOptional.orElse(new ArrayList<>());

                // Save input streams to files
                List<File> savedFiles = new ArrayList<>();
                for (Pair<InputStream, String> pair : migrationList) {
                    InputStream inputStream = pair.getKey();
                    String originalFileName = pair.getValue();
                    if (inputStream != null && originalFileName != null) {
                        // Save input stream to file
                        File outputFile = saveInputStreamToFile(inputStream, originalFileName);
                        if (outputFile != null) {
                            savedFiles.add(outputFile);
                        }
                    }
                }

                // Convert saved files to File objects
                filesToSave = savedFiles.toArray(new File[0]);
            } else {
                resourceUrl = Objects.requireNonNull(getClass().getResource("/assets/db/migration"));
                URI resourceUri = resourceUrl.toURI();
                if ("file".equals(resourceUri.getScheme())) {
                    filesToSave = new File(resourceUri).listFiles();
                } else {
                    // Handle case when resource is not found
                    System.out.println("Resource not found.");
                }
            }

            if (filesToSave != null) {
                for (File file : filesToSave) {
                    // Check if the file corresponds to a migration already applied

                    if (!isMigrationAlreadyApplied(file.getName(), appliedMigrations)) {

                        System.out.println("File '" + file.getName() + "' is not a migration already applied. Proceeding to save...");

                        try {
                            Path source = file.toPath();
                            Path destination = new File(customDirectory, file.getName()).toPath();

                            Files.copy(source, destination);
                        } catch (IOException ex) {
                            // Handle exception
                            ex.printStackTrace();
                        }
                    } else {
                        System.out.println("File '" + file.getName() + "' corresponds to a migration already applied. Skipping...");
                    }
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private List<String> getAppliedMigrationsFromDatabase() {
        List<String> appliedMigrations = new ArrayList<>();

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            // Load JDBC driver
            Class.forName(DRIVER);

            // Establish JDBC connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Create statement
            statement = connection.createStatement();

            // Execute SQL query to retrieve applied migrations
            resultSet = statement.executeQuery("SELECT * FROM \"flyway_schema_history\"");

            // Iterate through the result set and add applied migrations to the list
            while (resultSet.next()) {
                appliedMigrations.add(resultSet.getString("script"));
            }
        } catch (ClassNotFoundException | SQLException ex) {
            // Handle exception
            ex.printStackTrace();
        } finally {
            // Close resources
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    // Handle exception
                    e.printStackTrace();
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    // Handle exception
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // Handle exception
                    e.printStackTrace();
                }
            }
        }

        return appliedMigrations;
    }


    private boolean isMigrationAlreadyApplied(String fileName, List<String> appliedMigrations) {
        // Check if the migration corresponding to the file name is already applied
        return appliedMigrations.contains(fileName);
    }

    private void deleteFiles() {
        // Check Directory Existence
        File directory = new File(customDirectory);
        if (directory.exists()) {
            deleteFolder(directory);
        }
    }

    private void deleteFolder(File directory) {
        // Delete Files Under Directory
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        // Delete Directory.
        directory.delete();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public File getFile(String name) {
        String dir;
        File path;

        if (Platform.isAndroid()) {
            path = Services.get(StorageService.class).flatMap(StorageService::getPrivateStorage).orElseThrow(() -> new RuntimeException("Private storage is not accessible"));

        } else {
            dir = "Documents";

            path = Services.get(StorageService.class).flatMap(service -> service.getPublicStorage(dir)).orElseThrow(() -> new RuntimeException(dir + " is not available"));
        }

        return new File(path, name);
    }

    private File saveInputStreamToFile(InputStream inputStream, String fileName) {
        try {
            // Create a file with the original file name
            File outputFile = new File(customDirectory, fileName);

            // Write the input stream to the file
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            return outputFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}