package application;

// Represents a node in the Huffman Tree
public class HuffmanNode {

    // Byte value (0–255) for leaf nodes, or dummy value for internal nodes
    private int value;

    // Frequency of the byte or sum of frequencies for internal nodes
    private int frequency;

    // Left child in the Huffman Tree
    private HuffmanNode left;

    // Right child in the Huffman Tree
    private HuffmanNode right;

    // Constructor for leaf nodes (represents a real byte from the file)
    public HuffmanNode(int value, int frequency) {
        this.value = value;
        this.frequency = frequency;
    }

    // Constructor for internal nodes (created by merging two nodes)
    public HuffmanNode(int value, int frequency,
            HuffmanNode left, HuffmanNode right) {
        this.value = value;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    // Checks if this node is a leaf (no children)
    public boolean isLeaf() {
        return left == null && right == null;
    }

    // Returns the byte value of the node
    public int getValue() {
        return value;
    }

    // Returns the frequency of the node
    public int getFrequency() {
        return frequency;
    }

    // Returns the left child
    public HuffmanNode getLeft() {
        return left;
    }

    // Returns the right child
    public HuffmanNode getRight() {
        return right;
    }
}
