package application;

import java.net.URL;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI {
    private HuffmanTree huffmanTree;
    private final FrequencyCounter frequencyCounter = new FrequencyCounter();
    private Button compressBtn;
    private Button decompressBtn;
    private Tab huffmanCodes;
    private Tab headerCodes;
    private TextArea huffmanArea;
    private TextArea headerArea;
    private String headerText = "Compress or decompress a file to see operation details.";

    public void Interface1(Stage stage) {
        Label welcomeLabel = new Label("Huffman Coding");
        welcomeLabel.getStyleClass().add("title-label");

        TabPane tabPane = new TabPane();
        tabPane.setSide(Side.TOP);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        huffmanCodes = new Tab("Huffman Codes");
        headerCodes = new Tab("Header");

        huffmanArea = createOutputArea("Compress a file to display its Huffman codes.");
        headerArea = createOutputArea(headerText);
        huffmanCodes.setContent(huffmanArea);
        headerCodes.setContent(headerArea);
        tabPane.getTabs().addAll(huffmanCodes, headerCodes);

        compressBtn = new Button("Compress");
        decompressBtn = new Button("Decompress");
        compressBtn.setPrefWidth(150);
        decompressBtn.setPrefWidth(150);

        HBox buttons = new HBox(15, compressBtn, decompressBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(5, 0, 0, 0));

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        VBox root = new VBox(14, welcomeLabel, tabPane, buttons);
        root.setPadding(new Insets(18));
        root.setAlignment(Pos.TOP_CENTER);

        actions1();

        Scene scene = new Scene(root, 800, 600);
        URL stylesheet = GUI.class.getResource("/application/style.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle("Huffman Coding");
        stage.setMinWidth(640);
        stage.setMinHeight(480);
        stage.setMaximized(true);
        stage.show();
    }

    public void actions1() {
        huffmanCodes.setOnSelectionChanged(event -> {
            if (huffmanCodes.isSelected()) {
                updateHuffmanArea();
            }
        });

        headerCodes.setOnSelectionChanged(event -> {
            if (headerCodes.isSelected()) {
                headerArea.setText(headerText);
            }
        });

        compressBtn.setOnAction(event -> compressSelectedFile());
        decompressBtn.setOnAction(event -> decompressSelectedFile());
    }

    private TextArea createOutputArea(String initialText) {
        TextArea area = new TextArea(initialText);
        area.setEditable(false);
        area.setWrapText(false);
        return area;
    }

    private void compressSelectedFile() {
        if (!frequencyCounter.fileHandler()) {
            return;
        }

        int[] frequencies = frequencyCounter.buildFreq();
        if (frequencies == null) {
            return;
        }

        try {
            huffmanTree = new HuffmanTree(frequencies);
            huffmanTree.createHeap();
            huffmanTree.huffManTree();
            huffmanTree.generateCodes();

            HuffmanCompressor compressor = new HuffmanCompressor(frequencyCounter.getFileChosen(), frequencies);
            if (!compressor.writeHeaderToFile()
                    || !compressor.compressFile(huffmanTree.getCodes())) {
                showError("Compression Failed", compressor.getLastError());
                return;
            }

            updateHuffmanArea();
            headerText = compressor.getCompressionStats();
            headerArea.setText(headerText);
            showInformation(
                    "Compression Complete",
                    "File compressed successfully.",
                    compressor.getOutputFile().getAbsolutePath());
        } catch (RuntimeException exception) {
            showError("Compression Failed", readableMessage(exception));
        }
    }

    private void decompressSelectedFile() {
        HuffmanDecompressor decompressor = new HuffmanDecompressor();
        if (!decompressor.fileHandler()) {
            return;
        }
        if (!decompressor.decompress()) {
            showError("Decompression Failed", decompressor.getLastError());
            return;
        }

        huffmanTree = null;
        huffmanArea.setText("Compress a file to display its Huffman codes.");
        headerText = "Decompression completed successfully.\n"
                + "Input  : " + decompressor.getCompressedFile().getAbsolutePath() + "\n"
                + "Output : " + decompressor.getOutputFile().getAbsolutePath() + "\n";
        headerArea.setText(headerText);
        showInformation(
                "Decompression Complete",
                "File decompressed successfully.",
                decompressor.getOutputFile().getAbsolutePath());
    }

    private void updateHuffmanArea() {
        if (huffmanTree == null) {
            huffmanArea.setText("Compress a file to display its Huffman codes.");
        } else {
            huffmanArea.setText(huffmanTree.getCodesAsString());
        }
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message == null ? "The operation could not be completed." : message);
        alert.showAndWait();
    }

    private void showInformation(String header, String message, String path) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(message + "\n\nSaved to:\n" + path);
        alert.showAndWait();
    }

    private String readableMessage(Exception exception) {
        return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}
