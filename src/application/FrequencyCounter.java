package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

// This class handles file selection and byte frequency counting
public class FrequencyCounter {

    // The file selected by the user
    File fileChoosen;
    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
    Alert successAlert1 = new Alert(Alert.AlertType.INFORMATION);

    public File getFileChoosen() {
        return fileChoosen;
    }

    // Opens a file chooser and validates the selected file
    public boolean fileHandler() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a file to Compress");
        fileChooser.setInitialDirectory(new File("D:\\Coding\\Java\\FxHuffmanProject"));
        fileChoosen = fileChooser.showOpenDialog(null);

        if (fileChoosen == null) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("No file selected");
            errorAlert.setContentText("Please select a file.");
            errorAlert.showAndWait();
            return false;
        }
        // Prevent selecting already compressed files
        String fileName = fileChoosen.getName().toLowerCase();
        if (fileName.endsWith(".huf")) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Invalid File Extension");
            errorAlert.setContentText("This file is already compressed. Please select another file.");
            errorAlert.showAndWait();
            return false;
        }
        return true;
    }

    // Builds and returns the frequency array for all byte values (0–255)
    public int[] buildFreq() {
        if (fileChoosen == null) {
            return null;
        }
        // Array to store frequency of each byte
        int[] freqenucy = new int[256];
        // Buffer used to read multiple bytes at once
        byte[] buffBytes = new byte[8];
        try (FileInputStream fis = new FileInputStream(fileChoosen)) {
            int byteRead;
            // Read the file in chunks
            while ((byteRead = fis.read(buffBytes)) != -1) {
                for (int i = 0; i < byteRead; i++) {
                    // Convert signed byte to unsigned value (0–255)
                    int value = buffBytes[i] & 0xFF;
                    // Increase frequency for this byte

                    freqenucy[value]++;
                }
            }
        } catch (IOException e) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("File Read Error");
            errorAlert.setContentText("Cannot read the selected file.");
            errorAlert.showAndWait();
            return null;

        }
        return freqenucy;
    }
}
