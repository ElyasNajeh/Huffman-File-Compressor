package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

public class FrequencyCounter {
    private static final int BUFFER_SIZE = 8192;

    private File fileChosen;

    public File getFileChosen() {
        return fileChosen;
    }

    public boolean fileHandler() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a file to compress");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files", "*.*"));

        File homeDirectory = new File(System.getProperty("user.home", "."));
        if (homeDirectory.isDirectory()) {
            fileChooser.setInitialDirectory(homeDirectory);
        }

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            return false;
        }
        if (!selectedFile.isFile() || !selectedFile.canRead()) {
            showError("File Read Error", "The selected file cannot be read.");
            return false;
        }
        if (selectedFile.getName().toLowerCase().endsWith(".huf")) {
            showError("Invalid File Extension", "This file is already compressed. Select another file.");
            return false;
        }

        fileChosen = selectedFile;
        return true;
    }

    public int[] buildFreq() {
        if (fileChosen == null) {
            return null;
        }

        int[] frequencies = new int[256];
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalBytes = 0;

        try (FileInputStream input = new FileInputStream(fileChosen)) {
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                totalBytes += bytesRead;
                if (totalBytes > Integer.MAX_VALUE) {
                    throw new IOException("Files larger than 2 GB are not supported by this format.");
                }
                for (int index = 0; index < bytesRead; index++) {
                    int value = buffer[index] & 0xFF;
                    frequencies[value]++;
                }
            }
            return frequencies;
        } catch (IOException exception) {
            showError("File Read Error", exception.getMessage() == null
                    ? "Cannot read the selected file."
                    : exception.getMessage());
            return null;
        }
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
