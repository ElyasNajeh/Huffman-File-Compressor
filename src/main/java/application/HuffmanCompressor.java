package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

public class HuffmanCompressor {
    private final File inputFile;
    private final File outputFile;
    private final int[] frequencies;
    private int paddingBits;
    private String lastError;

    public HuffmanCompressor(File inputFile, int[] frequencies) {
        if (inputFile == null || !inputFile.isFile()) {
            throw new IllegalArgumentException("A valid input file is required.");
        }
        if (frequencies == null || frequencies.length != 256
                || Arrays.stream(frequencies).anyMatch(value -> value < 0)) {
            throw new IllegalArgumentException("A valid frequency array with 256 entries is required.");
        }

        this.inputFile = inputFile;
        this.frequencies = frequencies.clone();
        this.outputFile = new File(inputFile.getAbsolutePath() + ".huf");
    }

    public File getOutputFile() {
        return outputFile;
    }

    public String getLastError() {
        return lastError;
    }

    public boolean writeHeaderToFile() {
        try (FileOutputStream output = new FileOutputStream(outputFile)) {
            output.write(buildHeader().getBytes(StandardCharsets.US_ASCII));
            lastError = null;
            return true;
        } catch (IOException exception) {
            lastError = "Cannot write the compressed file: " + readableMessage(exception);
            deleteIncompleteOutput();
            return false;
        }
    }

    public boolean compressFile(String[] codes) {
        if (codes == null || codes.length != 256) {
            lastError = "The Huffman code table is invalid.";
            deleteIncompleteOutput();
            return false;
        }

        paddingBits = 0;
        StringBuilder bitsBuffer = new StringBuilder();

        try (FileInputStream input = new FileInputStream(inputFile);
                FileOutputStream output = new FileOutputStream(outputFile, true)) {
            int byteRead;
            while ((byteRead = input.read()) != -1) {
                String code = codes[byteRead];
                if (code == null) {
                    throw new IOException("The code table does not contain byte " + byteRead + ".");
                }
                bitsBuffer.append(code);

                while (bitsBuffer.length() >= 8) {
                    output.write(Integer.parseInt(bitsBuffer.substring(0, 8), 2));
                    bitsBuffer.delete(0, 8);
                }
            }

            if (!bitsBuffer.isEmpty()) {
                paddingBits = 8 - bitsBuffer.length();
                bitsBuffer.append("0".repeat(paddingBits));
                output.write(Integer.parseInt(bitsBuffer.toString(), 2));
            }

            output.write(paddingBits);
            lastError = null;
            return true;
        } catch (IOException | NumberFormatException exception) {
            lastError = "Cannot write the compressed file: " + readableMessage(exception);
            deleteIncompleteOutput();
            return false;
        }
    }

    public String getCompressionStats() {
        long originalSize = inputFile.length();
        long compressedSize = outputFile.length();
        double saving = originalSize == 0
                ? 0
                : (1 - ((double) compressedSize / originalSize)) * 100;

        return "Original Size   : " + originalSize + " bytes\n"
                + "Compressed Size : " + compressedSize + " bytes\n"
                + String.format("Saving          : %.2f %%\n", saving)
                + "Output          : " + outputFile.getAbsolutePath() + "\n";
    }

    private String buildHeader() {
        int symbolCount = 0;
        for (int frequency : frequencies) {
            if (frequency > 0) {
                symbolCount++;
            }
        }

        StringBuilder header = new StringBuilder().append(symbolCount).append('\n');
        for (int value = 0; value < frequencies.length; value++) {
            if (frequencies[value] > 0) {
                header.append(value).append(' ').append(frequencies[value]).append('\n');
            }
        }
        return header.toString();
    }

    private void deleteIncompleteOutput() {
        try {
            Files.deleteIfExists(outputFile.toPath());
        } catch (IOException ignored) {
            // The original error remains more useful to the caller.
        }
    }

    private String readableMessage(Exception exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
