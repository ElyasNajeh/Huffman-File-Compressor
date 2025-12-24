package application;

// This class is responsible for building the Huffman Tree and generating codes
public class HuffmanTree {

    // Used to get byte frequencies from the selected file
    private FrequencyCounter frequencyCounter;

    // Root of the Huffman Tree
    HuffmanNode root;

    // MinHeap used to build the Huffman Tree
    MinHeap heap;

    // Stores Huffman codes for each byte (0–255)
    String[] codes = new String[256];

    // Stores frequency of each byte
    int[] freq;

    // Constructor receives the frequency counter
    public HuffmanTree(FrequencyCounter frequencyCounter) {
        this.frequencyCounter = frequencyCounter;
    }

    // Returns the root of the Huffman Tree
    public HuffmanNode getRoot() {
        return root;
    }

    // Returns the generated Huffman codes
    public String[] getCodes() {
        return codes;
    }

    // Creates the heap and inserts all leaf nodes
    public void createHeap() {

        // Build frequency array
        freq = frequencyCounter.buildFreq();

        // Count how many different bytes exist
        int count = 0;
        for (int i = 0; i < 256; i++) {
            if (freq[i] > 0)
                count++;
        }

        // Create heap with exact size
        heap = new MinHeap(count);

        // Insert all leaf nodes into the heap
        for (int i = 0; i < 256; i++) {
            if (freq[i] > 0) {
                HuffmanNode node = new HuffmanNode(i, freq[i]);
                heap.insert(node);
            }
        }
    }

    // Builds the Huffman Tree using the MinHeap
    public void huffManTree() {

        // Repeat until only one node remains
        while (heap.getCapacity() > 1) {

            // Extract two nodes with minimum frequency
            HuffmanNode node1 = heap.extractMin();
            HuffmanNode node2 = heap.extractMin();

            HuffmanNode left;
            HuffmanNode right;

            // Determine left and right child based on frequency
            if (node1.getFrequency() < node2.getFrequency()) {
                left = node1;
                right = node2;
            } else if (node1.getFrequency() > node2.getFrequency()) {
                left = node2;
                right = node1;
            } else {
                // If frequencies are equal, compare by value
                if (node1.getValue() <= node2.getValue()) {
                    left = node1;
                    right = node2;
                } else {
                    left = node2;
                    right = node1;
                }
            }

            // Create parent node with combined frequency
            HuffmanNode parent = new HuffmanNode(
                    -1,
                    left.getFrequency() + right.getFrequency(),
                    left,
                    right);

            // Insert parent back into the heap
            heap.insert(parent);
        }

        // The last remaining node is the root
        root = heap.extractMin();
    }

    // Recursively generates Huffman codes
    private void generateCodes(HuffmanNode node, String code) {

        if (node == null)
            return;

        // If leaf node, store the code
        if (node.isLeaf()) {
            codes[node.getValue()] = code.length() > 0 ? code : "0";
            return;
        }

        // Traverse left with '0'
        generateCodes(node.getLeft(), code + "0");

        // Traverse right with '1'
        generateCodes(node.getRight(), code + "1");
    }

    // Starts code generation from the root
    public void generateCodes() {
        generateCodes(root, "");
    }

    // Returns all Huffman codes as a formatted string
    public String getCodesAsString() {

        String result = "Huffman Codes \n";
        result += "---------------------------------------------------------\n";

        for (int i = 0; i < 256; i++) {
            if (codes[i] != null) {
                result += String.format(
                        "Byte %-3d | Frequency: %-6d | Huffman Code: %-15s | Length: %-2d%n",
                        i, freq[i], codes[i], codes[i].length());
            }
        }

        result += "---------------------------------------------------------\n";
        return result;
    }
}
