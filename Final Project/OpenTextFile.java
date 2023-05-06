package com.example.finalproject;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OpenTextFile extends Application {

    public ProgressBar progressBar;
    private Button openButton;
    private TextArea textArea;
    private Label statusLabel;
    private SortTask sortTask;


    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("File Sorter");

        // Create UI controls
        statusLabel = new Label("Click the 'Open File' button to select a file to sort.");
        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        openButton = new Button("Open File");
        openButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                sortFile(file);
            }
        });
        textArea = new TextArea();


        // Create UI layout
        VBox root = new VBox(10, statusLabel, progressBar, openButton, textArea);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.show();


    }
    private void sortFile(File inputFile) {
        // Disable the open button while the file is being sorted
        openButton.setDisable(true);

        // Create and start the SortTask in a separate thread
        sortTask = new SortTask(inputFile);
        Thread thread = new Thread(sortTask);
        thread.setDaemon(true);
        thread.start();

        // Bind the progress bar to the progress of the SortTask
        progressBar.progressProperty().bind(sortTask.progressProperty());

        // Update the UI with the progress of the SortTask
        statusLabel.setText("Sorting file...");
        progressBar.setVisible(true);
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(sortTask.progressProperty());

        // Handle the completion of the SortTask
        sortTask.setOnSucceeded(event -> {
            List<String> sortedLines = sortTask.getValue();
            StringBuilder sb = new StringBuilder();
            for (String line : sortedLines) {
                sb.append(line).append("\n");
            }
            statusLabel.setText("File sorted successfully!");
            textArea.setText(sb.toString());
            openButton.setDisable(false);
            progressBar.setVisible(false);
        });

        sortTask.setOnFailed(event -> {
            progressBar.setVisible(false);
            openButton.setDisable(false);
            showAlert(Alert.AlertType.ERROR, "Failed to sort file: " + sortTask.getException().getMessage());
        });
    }


    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static class SortTask extends Task<List<String>> {
        private final File inputFile;

        public SortTask(File inputFile) {
            this.inputFile = inputFile;
        }

        @Override
        protected List<String> call() throws Exception {
            // Read the contents of the file into a List
            List<String> lines = Files.readAllLines(inputFile.toPath(), StandardCharsets.UTF_8);

            // Remove any whitespace from each line
            List<String> trimmedLines = lines.stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());

            // Sort the non-numeric lines using natural ordering
            List<String> sortedLines = trimmedLines.stream()
                    .filter(line -> !line.matches("-?\\d+"))
                    .sorted()
                    .collect(Collectors.toList());

            // Append the numeric lines to the sorted non-numeric lines
            List<String> numericLines = trimmedLines.stream()
                    .filter(line -> line.matches("-?\\d+"))
                    .collect(Collectors.toList());
            sortedLines.addAll(numericLines);

            // Return the sorted lines
            return sortedLines;
        }
    }




    public static void main(String[] args) {
        launch(args);
    }
}
