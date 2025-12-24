package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

// Main GUI class for Huffman Compression and Decompression
public class GUI {

    // Huffman tree used to generate codes
    private HuffmanTree huffmanTree;

    // Handles file selection and frequency counting
    private FrequencyCounter frequencyCounter = new FrequencyCounter();

    // Handles file compression
    private HuffmanCompressor huffmanCompressor;

    // Buttons for compressing and decompressing
    private Button compressBtn, decompressBtn;

    // Tabs to display Huffman codes and header information
    private Tab huffmanCodes, headerCodes;

    // Text areas to display codes and header details
    TextArea huffmanArea, headerArea;

    // Stores compression statistics text
    String resHeader = "";

    // Alert used to show success messages
    private Alert okAlert = new Alert(Alert.AlertType.INFORMATION);

    // Builds and displays the main interface
    public void Interface1(Stage stage) {

        // Title label
        Label welcomeLabel = new Label("Huffman Coding");

        // Tab pane to hold Huffman Codes and Header tabs
        TabPane tabPane = new TabPane();
        tabPane.setSide(Side.TOP);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tabs initialization
        huffmanCodes = new Tab("Huffman Codes");
        headerCodes = new Tab("Header");

        // Text area to show Huffman codes
        huffmanArea = new TextArea();
        huffmanArea.setEditable(false);
        huffmanArea.setWrapText(true);
        huffmanCodes.setContent(huffmanArea);

        // Text area to show header or compression statistics
        headerArea = new TextArea();
        headerArea.setEditable(false);
        headerArea.setWrapText(true);
        headerCodes.setContent(headerArea);

        // Add tabs to tab pane
        tabPane.getTabs().addAll(huffmanCodes, headerCodes);

        // Initialize buttons
        compressBtn = new Button("Compress");
        decompressBtn = new Button("Decompress");

        // Layout for buttons
        HBox buttons = new HBox(15, compressBtn, decompressBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(10));

        // Scroll pane to contain buttons
        ScrollPane bottomScroll = new ScrollPane(buttons);
        bottomScroll.setFitToWidth(true);
        bottomScroll.setPannable(true);

        // Allow tab pane to grow vertically
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        // Root layout
        VBox root = new VBox(10);
        root.getChildren().addAll(welcomeLabel, tabPane, bottomScroll);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.TOP_CENTER);

        // Attach actions to UI elements
        actions1();

        // Scene setup
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Huffman Coding");
        stage.setMaximized(true);

        // Load CSS styling
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        // Show stage
        stage.show();
    }

    // Defines actions for tabs and buttons
    public void actions1() {

        // When Huffman Codes tab is selected
        huffmanCodes.setOnSelectionChanged(x -> {
            if (huffmanCodes.isSelected()) {
                if (huffmanTree == null) {
                    huffmanArea.setText("Please Compress File First.");
                    return;
                }
                huffmanArea.setText(huffmanTree.getCodesAsString());
            }
        });

        // When Header tab is selected
        headerCodes.setOnSelectionChanged(x -> {
            if (headerCodes.isSelected()) {
                if (huffmanCompressor == null) {
                    huffmanArea.setText("Please Compress or Decmpress File First.");
                    return;
                }
                resHeader = huffmanCompressor.getCompressionStats();
                headerArea.setText(resHeader);
            }
        });

        // Compress button action
        compressBtn.setOnAction(x -> {

            // Let user choose file
            boolean choose = frequencyCounter.fileHandler();
            if (!choose) {
                return;
            }

            // Build frequency array
            int[] freq = frequencyCounter.buildFreq();

            // Initialize compressor
            huffmanCompressor = new HuffmanCompressor(frequencyCounter.getFileChoosen(), freq);

            // Reset Huffman tree
            huffmanTree = null;

            if (huffmanTree == null) {

                // Build Huffman tree and codes
                huffmanTree = new HuffmanTree(frequencyCounter);
                huffmanTree.createHeap();
                huffmanTree.huffManTree();
                huffmanTree.generateCodes();

                // Write header and compress file
                huffmanCompressor.writeHeaderToFile();
                huffmanCompressor.compressFile(huffmanTree.getCodes());

                // Show success message
                okAlert.setTitle("Success");
                okAlert.setHeaderText("Compression Done");
                okAlert.setContentText("File compressed successfully.");
                okAlert.showAndWait();
            }
        });

        // Decompress button action
        decompressBtn.setOnAction(x -> {

            // Create decompressor
            HuffmanDecompressor d = new HuffmanDecompressor();

            // Let user choose compressed file
            if (!d.fileHandler())
                return;

            // Perform decompression
            d.decompress();

            // Show success message
            okAlert.setTitle("Success");
            okAlert.setHeaderText("Decompression Done");
            okAlert.setContentText("File decompressed successfully.");
            okAlert.showAndWait();
        });
    }
}
