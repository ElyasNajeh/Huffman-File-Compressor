package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.scene.control.Alert;

// This class handles the compression process using Huffman coding
public class HuffmanCompressor {

    // Original input file
    private File inputFile;

    // Output compressed file (.huf)
    private File outputFile;

    // Frequency array for all bytes
    private int[] frequencies;

    // Number of padding bits added at the end
    private int paddingBits;

    // Alert used to display error messages
    Alert errorAlert = new Alert(Alert.AlertType.ERROR);

    // Constructor initializes files and frequency array
    public HuffmanCompressor(File inputFile, int[] frequencies) {
        this.inputFile = inputFile;
        this.frequencies = frequencies;
        this.outputFile = new File(inputFile.getAbsolutePath() + ".huf");
    }

    // Builds the header that stores frequency information
    private String buildHeader() {

        // Count number of distinct bytes to know when we should stop in decompress
        int count = 0;
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                count++;
            }
        }

        // Build header as text
        String header = "";
        header += count + "\n";

        // Add byte value and its frequency
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                header += i + " " + frequencies[i] + "\n";
            }
        }

        return header;
    }

    // Writes the header to the compressed file
    public void writeHeaderToFile() {

        String header = buildHeader();

        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(header.getBytes());
        } catch (IOException e) {

            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("File Write Error");
            errorAlert.setContentText("Cannot Write in the file.");
            errorAlert.showAndWait();
            return;
        }
    }

    // Compresses the input file using Huffman codes
    public void compressFile(String[] codes) {

        // Buffer to store bits before writing them as bytes
        StringBuilder bitsBuffer = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(inputFile);
                FileOutputStream fos = new FileOutputStream(outputFile, true);) {

            int byteRead;

            // Read the file byte by byte
            while ((byteRead = fis.read()) != -1) {

                // Append Huffman code for the read byte
                if (codes[byteRead] != null) {
                    bitsBuffer.append(codes[byteRead]);
                }

                // Write bytes whenever we have at least 8 bits
                while (bitsBuffer.length() >= 8) {

                    String byteS = bitsBuffer.substring(0, 8);
                    bitsBuffer.delete(0, 8);

                    // Convert binary string to integer
                    int value = Integer.parseInt(byteS, 2);
                    fos.write(value);
                }
            }

            // Handle remaining bits (padding)
            if (bitsBuffer.length() > 0) {

                paddingBits = 0;

                // Pad remaining bits with zeros
                while (bitsBuffer.length() < 8) {
                    bitsBuffer.append("0");
                    paddingBits++;
                }

                int value = Integer.parseInt(bitsBuffer.toString(), 2);
                fos.write(value);
            }

            // Write number of padding bits at the end of file
            fos.write(paddingBits);

        } catch (IOException e) {

            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("File Write Error");
            errorAlert.setContentText("Cannot Write in the file.");
            errorAlert.showAndWait();
            return;
        }
    }

    // Returns compression statistics
    public String getCompressionStats() {

        long originalSize = inputFile.length();
        long compressedSize = outputFile.length();

        double saving = 0;
        if (originalSize > 0) {
            saving = (1 - ((double) compressedSize / originalSize)) * 100;
        }

        String result = "";
        result += "Original Size   : " + originalSize + " bytes\n";
        result += "Compressed Size : " + compressedSize + " bytes\n";
        result += String.format("Saving          : %.2f %%\n", saving);

        return result;
    }
}
