package application;

public class HuffmanNode {
    private final int value;
    private final int frequency;
    private final HuffmanNode left;
    private final HuffmanNode right;

    public HuffmanNode(int value, int frequency) {
        this(value, frequency, null, null);
    }

    public HuffmanNode(int value, int frequency, HuffmanNode left, HuffmanNode right) {
        this.value = value;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public int getValue() {
        return value;
    }

    public int getFrequency() {
        return frequency;
    }

    public HuffmanNode getLeft() {
        return left;
    }

    public HuffmanNode getRight() {
        return right;
    }
}
