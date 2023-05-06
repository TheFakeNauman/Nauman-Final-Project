package com.example.finalproject;

import javafx.application.Application;

import java.io.*;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class HelloApplication extends Application {

    private static ProgressBar progressBarTest = new ProgressBar(0);
    private static Task<Void> task;

    @Override
    public void start(Stage primaryStage) throws IOException {
        TextArea area = new TextArea();
        BorderPane root = new BorderPane();
        Button btn = new Button("Select File");
        Label label = new Label();
        Button submit = new Button();
        Button clear = new Button();
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                // Create a FileChooser object to prompt the user to select a file
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select input file");

                // Add a filter to limit the selection to text files
                FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
                fileChooser.getExtensionFilters().add(textFilter);

                // Display the file chooser and get the selected file
                File inputFile = fileChooser.showOpenDialog(primaryStage);

                if (inputFile != null) {

                    double numberLines = 0;
                    AtomicReference<Double> currentLine = new AtomicReference<>((double) 0);
                    final double[] progress = {0};

                    //Getting the number of lines
                    Scanner scanner1;
                    try {
                        scanner1 = new Scanner(inputFile);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                    while (scanner1.hasNextLine()) {
                        String line = scanner1.nextLine();
                        numberLines++;
                    }
                    scanner1.close();

//                    Sorting and updating the progress bar
                    Scanner scanner2;
                    try {
                        scanner2 = new Scanner(inputFile);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }


                    ArrayList<String> arrayList = new ArrayList<String>();

                    double finalNumberLines = numberLines;
                    Thread addingThread = new Thread(() -> {
                        final double[] g = {0};
                        while (scanner2.hasNextLine()) {
                            currentLine.getAndSet(new Double((double) (currentLine.get() + 1)));
                            arrayList.add(scanner2.nextLine());
                            task = new Task<Void>() {
                                @Override
                                protected Void call() throws Exception {
                                    progressBarTest.setProgress(progress[0]);
                                    updateProgress(progress[0], 1.0);
                                    g[0] = g[0] + 0.1;
                                    progress[0] = currentLine.get() / finalNumberLines;
                                    Thread.sleep(3);
                                    return null;
                                }
                            };
                            task.run();
                        }

                        ArrayList<String> wordList = new ArrayList<String>();

                        //Iterate through input line
                        for (String line : arrayList){
                            //Split line into words
                            String[] words = line.split(" ");
                            //Add each word to wordList
                            for(String word : words) {
                                if(!word.isEmpty()) {
                                    wordList.add(word);
                                }
                            }
                        }

                        submit.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                String searchTerm = area.getText();
                                searchTerm = " " + searchTerm + " ";

                                String fullLine = "";
                                for(int i = 0;i < arrayList.size();i++) {
                                    String line = arrayList.get(i);
                                    if (line.toLowerCase().contains(searchTerm)) {
                                        fullLine = fullLine + "Line " + (i + 1) + ": " + line + "\n";

                                    }else{
                                        area.setText("NO RESULTS");
                                    }

                                }
                                area.setText(fullLine);
                            }
                        });
                        clear.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                area.setText("");
                            }
                        });

                    });

                    addingThread.start();

                }


            }
        });
        label.setText("Please enter a search term:");
        submit.setText("Submit");
        clear.setText("Reset");
        VBox vbox = new VBox();
        HBox hbox = new HBox();
        vbox.setSpacing(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(btn, progressBarTest,label, area);
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);
        hbox.getChildren().addAll(submit,clear);
        Scene scene = new Scene(root, 500, 500);
        root.setPadding(new Insets(50));
        root.setCenter(vbox);
        root.setBottom(hbox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("File Checker");
        primaryStage.show();
    }



    public static void main(String[] args) {
        launch();

    }
}