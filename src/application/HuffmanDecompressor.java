package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

// This class handles Huffman decompression process
public class HuffmanDecompressor {

    // Compressed input file (.huf)
    private File compressedFile;

    // Output file after decompression
    private File outputFile;

    // Frequency array reconstructed from the header
    private int[] frequencies = new int[256];

    // Number of padding bits added at the end of file
    private int paddingBits;

    // Alert used to display error messages
    Alert errorAlert = new Alert(Alert.AlertType.ERROR);

    // Returns the selected compressed file
    public File getCompressedFile() {
        return compressedFile;
    }

    // Handles file selection and validation
    public boolean fileHandler() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a file to DeCompress");
        fileChooser.setInitialDirectory(new File("D:\\Coding\\Java\\FxHuffmanProject"));
        compressedFile = fileChooser.showOpenDialog(null);

        // No file selected
        if (compressedFile == null) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("No file selected");
            errorAlert.setContentText("Please select a file.");
            errorAlert.showAndWait();
            return false;
        }

        // Check file extension
        String fileName = compressedFile.getName().toLowerCase();
        if (!fileName.endsWith(".huf")) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Invalid File Extension");
            errorAlert.setContentText(
                    "This file isn't Compressed So you Cant to DeCompress. Please select another file ends with .huf");
            errorAlert.showAndWait();
            return false;
        }

        // Create output file name
        outputFile = createOutputFile(compressedFile);
        return true;
    }

    // Creates output file by removing .huf extension
    private File createOutputFile(File hufFile) {

        String name = hufFile.getAbsolutePath();
        name = name.substring(0, name.length() - 4);
        return new File(name);
    }

    // Reads the header and fills the frequency array
    private void readHeader() {

        try (FileInputStream fis = new FileInputStream(compressedFile)) {

            StringBuilder line = new StringBuilder();
            int b;

            // Read number of distinct symbols
            while ((b = fis.read()) != '\n') {
                line.append((char) b);
            }

            int symbols = Integer.parseInt(line.toString().trim());
            line.setLength(0);

            // Read each symbol and its frequency
            for (int i = 0; i < symbols; i++) {

                while ((b = fis.read()) != '\n') {
                    line.append((char) b);
                }

                String[] parts = line.toString().trim().split("\\s+");
                int value = Integer.parseInt(parts[0].trim());
                int freq = Integer.parseInt(parts[1].trim());

                frequencies[value] = freq;
                line.setLength(0);
            }

        } catch (IOException e) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Header Read Error");
            errorAlert.setContentText("Cannot read header from file.");
            errorAlert.showAndWait();
        }
    }

    // Rebuilds the Huffman Tree using the frequency array
    private HuffmanNode rebuildTree() {

        MinHeap heap;
        int count = 0;

        // Count number of symbols
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0)
                count++;
        }

        heap = new MinHeap(count);

        // Insert leaf nodes
        for (int i = 0; i < 256; i++) {
            if (frequencies[i] > 0) {
                heap.insert(new HuffmanNode(i, frequencies[i]));
            }
        }

        // Build the Huffman Tree
        while (heap.getCapacity() > 1) {

            HuffmanNode node1 = heap.extractMin();
            HuffmanNode node2 = heap.extractMin();

            HuffmanNode left;
            HuffmanNode right;

            // Determine left and right child
            if (node1.getFrequency() < node2.getFrequency()) {
                left = node1;
                right = node2;
            } else if (node1.getFrequency() > node2.getFrequency()) {
                left = node2;
                right = node1;
            } else {
                // Break ties using value
                if (node1.getValue() <= node2.getValue()) {
                    left = node1;
                    right = node2;
                } else {
                    left = node2;
                    right = node1;
                }
            }

            // Create parent node
            HuffmanNode parent = new HuffmanNode(
                    -1, left.getFrequency() + right.getFrequency(), left, right);

            heap.insert(parent);
        }

        // Return root of the tree
        return heap.extractMin();
    }

    // Performs the decompression process
    public void decompress() {

        readHeader();
        HuffmanNode root = rebuildTree();
        HuffmanNode current = root;

        // Validate tree
        if (root == null) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Tree Error");
            errorAlert.setContentText("Cannot rebuild Huffman tree (empty or invalid file).");
            errorAlert.showAndWait();
            return;
        }

        // Special case: only one unique byte
        if (root.isLeaf()) {

            int total = root.getFrequency();

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                for (int i = 0; i < total; i++)
                    fos.write(root.getValue() & 0xFF);
            } catch (IOException e) {
                errorAlert.setTitle("Error");
                errorAlert.setHeaderText("Decompression Error");
                errorAlert.setContentText("Cannot decompress file.");
                errorAlert.showAndWait();
            }
            return;
        }

        // Read padding information
        readPadding();

        try (FileInputStream fis = new FileInputStream(compressedFile);
                FileOutputStream fos = new FileOutputStream(outputFile)) {

            StringBuilder line = new StringBuilder();
            int b;

            // Skip first line (symbols count)
            while ((b = fis.read()) != '\n') {
            }

            // Skip symbol-frequency lines
            for (int i = 0; i < countSymbols(); i++) {
                while ((b = fis.read()) != '\n') {
                }
            }

            long dataSize = compressedFile.length();
            long currentPos = fis.getChannel().position();
            long bytesLeft = dataSize - currentPos - 1;

            // Read compressed data bytes
            while (bytesLeft-- > 0) {

                int data = fis.read();

                // Determine how many bits to read
                int limit = (bytesLeft == 0) ? 8 - paddingBits : 8;

                // Process each bit
                for (int i = 7; i >= 8 - limit; i--) {

                    int bit = (data >> i) & 1;
                    current = (bit == 0) ? current.getLeft() : current.getRight();

                    // If leaf reached, write byte
                    if (current.isLeaf()) {
                        fos.write(current.getValue());
                        current = root;
                    }
                }
            }

        } catch (IOException e) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Decompression Error");
            errorAlert.setContentText("Cannot decompress file.");
            errorAlert.showAndWait();
        }
    }

    // Counts number of symbols in frequency array
    private int countSymbols() {

        int c = 0;
        for (int i = 0; i < 256; i++)
            if (frequencies[i] > 0)
                c++;
        return c;
    }

    // Reads padding bits count from the last byte of the file
    private void readPadding() {

        try (FileInputStream fis = new FileInputStream(compressedFile)) {
            fis.getChannel().position(compressedFile.length() - 1);
            paddingBits = fis.read();
        } catch (IOException e) {
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Padding Error");
            errorAlert.setContentText("Cannot Read Padding.");
            errorAlert.showAndWait();
        }
    }
}
