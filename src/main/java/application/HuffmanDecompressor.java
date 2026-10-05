package application;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;

public class HuffmanDecompressor {
    private static final int MAX_HEADER_LINE_LENGTH = 64;

    private File compressedFile;
    private File outputFile;
    private final int[] frequencies = new int[256];
    private int paddingBits;
    private String lastError;

    public HuffmanDecompressor() {
    }

    public HuffmanDecompressor(File compressedFile) {
        if (!setCompressedFile(compressedFile)) {
            throw new IllegalArgumentException(lastError);
        }
    }

    public File getCompressedFile() {
        return compressedFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean fileHandler() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a file to decompress");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Huffman files", "*.huf"));

        File homeDirectory = new File(System.getProperty("user.home", "."));
        if (homeDirectory.isDirectory()) {
            fileChooser.setInitialDirectory(homeDirectory);
        }

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile == null) {
            return false;
        }
        if (!setCompressedFile(selectedFile)) {
            showError("Invalid Compressed File", lastError);
            return false;
        }
        return true;
    }

    public boolean decompress() {
        lastError = null;
        if (compressedFile == null || outputFile == null) {
            return fail("No compressed file has been selected.");
        }

        long headerEnd = readHeader();
        if (headerEnd < 0) {
            return false;
        }
        if (!readPadding()) {
            return false;
        }

        long payloadSize = compressedFile.length() - headerEnd - 1;
        if (payloadSize < 0) {
            return fail("The compressed file is truncated.");
        }

        long expectedBytes = Arrays.stream(frequencies).asLongStream().sum();
        int symbolCount = countSymbols();
        if (symbolCount == 0) {
            if (payloadSize != 0 || paddingBits != 0) {
                return fail("The compressed file has invalid empty-file data.");
            }
            return writeEmptyOutput();
        }

        HuffmanNode root = rebuildTree();
        if (root == null) {
            return fail("Cannot rebuild the Huffman tree.");
        }

        if (root.isLeaf()) {
            long requiredPayloadSize = (expectedBytes + 7) / 8;
            int expectedPadding = (int) ((8 - (expectedBytes % 8)) % 8);
            if (payloadSize != requiredPayloadSize || paddingBits != expectedPadding) {
                return fail("The compressed data does not match its header.");
            }
            return writeRepeatedByte(root.getValue(), expectedBytes);
        }

        if (payloadSize == 0 || (paddingBits > 0 && payloadSize < 1)) {
            return fail("The compressed data is missing.");
        }
        return decodePayload(root, headerEnd, payloadSize, expectedBytes);
    }

    private boolean setCompressedFile(File file) {
        if (file == null || !file.isFile() || !file.canRead()) {
            lastError = "The selected file cannot be read.";
            return false;
        }
        String fileName = file.getName();
        if (!fileName.toLowerCase().endsWith(".huf") || fileName.length() <= 4) {
            lastError = "Select a valid file whose name ends with .huf.";
            return false;
        }

        compressedFile = file;
        outputFile = createOutputFile(file);
        lastError = null;
        return true;
    }

    private long readHeader() {
        Arrays.fill(frequencies, 0);

        try (FileInputStream input = new FileInputStream(compressedFile)) {
            int symbolCount = parseInteger(readAsciiLine(input), "symbol count");
            if (symbolCount < 0 || symbolCount > 256) {
                throw new IOException("The symbol count must be between 0 and 256.");
            }

            boolean[] seen = new boolean[256];
            long totalFrequency = 0;
            for (int index = 0; index < symbolCount; index++) {
                String[] parts = readAsciiLine(input).trim().split("\\s+");
                if (parts.length != 2) {
                    throw new IOException("A symbol-frequency entry is malformed.");
                }

                int value = parseInteger(parts[0], "byte value");
                int frequency = parseInteger(parts[1], "frequency");
                if (value < 0 || value > 255 || frequency <= 0 || seen[value]) {
                    throw new IOException("The header contains an invalid or duplicate byte entry.");
                }

                totalFrequency += frequency;
                if (totalFrequency > Integer.MAX_VALUE) {
                    throw new IOException("The uncompressed size exceeds the supported 2 GB limit.");
                }
                seen[value] = true;
                frequencies[value] = frequency;
            }

            long headerEnd = input.getChannel().position();
            if (headerEnd >= compressedFile.length()) {
                throw new IOException("The compressed file is missing its padding byte.");
            }
            return headerEnd;
        } catch (IOException | NumberFormatException exception) {
            fail("Cannot read the compressed-file header: " + readableMessage(exception));
            return -1;
        }
    }

    private String readAsciiLine(FileInputStream input) throws IOException {
        StringBuilder line = new StringBuilder();
        while (true) {
            int value = input.read();
            if (value == -1) {
                throw new EOFException("Unexpected end of header.");
            }
            if (value == '\n') {
                return line.toString();
            }
            if (value != '\r') {
                if (value < 32 || value > 126 || line.length() >= MAX_HEADER_LINE_LENGTH) {
                    throw new IOException("The header contains invalid text.");
                }
                line.append((char) value);
            }
        }
    }

    private int parseInteger(String text, String fieldName) throws IOException {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException exception) {
            throw new IOException("Invalid " + fieldName + ".", exception);
        }
    }

    private HuffmanNode rebuildTree() {
        MinHeap heap = new MinHeap(countSymbols());
        for (int value = 0; value < frequencies.length; value++) {
            if (frequencies[value] > 0) {
                heap.insert(new HuffmanNode(value, frequencies[value]));
            }
        }

        while (heap.getCapacity() > 1) {
            HuffmanNode first = heap.extractMin();
            HuffmanNode second = heap.extractMin();
            HuffmanNode left;
            HuffmanNode right;

            if (first.getFrequency() < second.getFrequency()
                    || (first.getFrequency() == second.getFrequency()
                            && first.getValue() <= second.getValue())) {
                left = first;
                right = second;
            } else {
                left = second;
                right = first;
            }

            heap.insert(new HuffmanNode(
                    -1,
                    left.getFrequency() + right.getFrequency(),
                    left,
                    right));
        }
        return heap.extractMin();
    }

    private boolean readPadding() {
        if (compressedFile.length() == 0) {
            return fail("The compressed file is empty.");
        }

        try (FileInputStream input = new FileInputStream(compressedFile)) {
            input.getChannel().position(compressedFile.length() - 1);
            paddingBits = input.read();
            if (paddingBits < 0 || paddingBits > 7) {
                return fail("The padding value must be between 0 and 7.");
            }
            return true;
        } catch (IOException exception) {
            return fail("Cannot read the padding value: " + readableMessage(exception));
        }
    }

    private boolean decodePayload(HuffmanNode root, long headerEnd, long payloadSize, long expectedBytes) {
        long writtenBytes = 0;
        HuffmanNode current = root;

        try (FileInputStream input = new FileInputStream(compressedFile);
                FileOutputStream output = new FileOutputStream(outputFile)) {
            input.getChannel().position(headerEnd);

            for (long payloadIndex = 0; payloadIndex < payloadSize; payloadIndex++) {
                int data = input.read();
                if (data == -1) {
                    throw new EOFException("Unexpected end of compressed data.");
                }

                int bitsToRead = payloadIndex == payloadSize - 1 ? 8 - paddingBits : 8;
                for (int bitIndex = 7; bitIndex >= 8 - bitsToRead; bitIndex--) {
                    int bit = (data >> bitIndex) & 1;
                    current = bit == 0 ? current.getLeft() : current.getRight();
                    if (current == null) {
                        throw new IOException("The compressed bit stream is invalid.");
                    }
                    if (current.isLeaf()) {
                        if (writtenBytes >= expectedBytes) {
                            throw new IOException("The compressed data exceeds the size declared in its header.");
                        }
                        output.write(current.getValue());
                        writtenBytes++;
                        current = root;
                    }
                }
            }

            if (current != root || writtenBytes != expectedBytes) {
                throw new IOException("The compressed data is incomplete or does not match its header.");
            }
            lastError = null;
            return true;
        } catch (IOException exception) {
            deleteIncompleteOutput();
            return fail("Cannot decompress the file: " + readableMessage(exception));
        }
    }

    private boolean writeEmptyOutput() {
        try (FileOutputStream ignored = new FileOutputStream(outputFile)) {
            lastError = null;
            return true;
        } catch (IOException exception) {
            deleteIncompleteOutput();
            return fail("Cannot create the output file: " + readableMessage(exception));
        }
    }

    private boolean writeRepeatedByte(int value, long count) {
        try (FileOutputStream output = new FileOutputStream(outputFile)) {
            for (long index = 0; index < count; index++) {
                output.write(value);
            }
            lastError = null;
            return true;
        } catch (IOException exception) {
            deleteIncompleteOutput();
            return fail("Cannot decompress the file: " + readableMessage(exception));
        }
    }

    private int countSymbols() {
        int count = 0;
        for (int frequency : frequencies) {
            if (frequency > 0) {
                count++;
            }
        }
        return count;
    }

    private File createOutputFile(File hufFile) {
        String sourcePath = hufFile.getAbsolutePath();
        File preferred = new File(sourcePath.substring(0, sourcePath.length() - 4));
        if (!preferred.exists()) {
            return preferred;
        }

        File parent = preferred.getParentFile();
        String name = preferred.getName();
        int extensionIndex = name.lastIndexOf('.');
        String stem = extensionIndex > 0 ? name.substring(0, extensionIndex) : name;
        String extension = extensionIndex > 0 ? name.substring(extensionIndex) : "";

        File candidate = new File(parent, stem + "-decompressed" + extension);
        int copyNumber = 2;
        while (candidate.exists()) {
            candidate = new File(parent, stem + "-decompressed-" + copyNumber + extension);
            copyNumber++;
        }
        return candidate;
    }

    private boolean fail(String message) {
        lastError = message;
        return false;
    }

    private void deleteIncompleteOutput() {
        try {
            Files.deleteIfExists(outputFile.toPath());
        } catch (IOException ignored) {
            // Preserve the decompression error for the caller.
        }
    }

    private String readableMessage(Exception exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
