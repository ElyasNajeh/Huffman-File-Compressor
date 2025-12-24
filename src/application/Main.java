package application;

import javafx.application.Application;
import javafx.stage.Stage;

// Main entry point for the JavaFX application
public class Main extends Application {

    // This method is called automatically when the application starts
    @Override
    public void start(Stage stage) {

        // Create GUI object
        GUI gui = new GUI();

        // Initialize and show the main interface
        gui.Interface1(stage);
    }

    // Launches the JavaFX application
    public static void main(String[] args) {
        launch(args);
    }
}
