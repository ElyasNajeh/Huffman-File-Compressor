package application;

import java.util.Arrays;

public class HuffmanTree {
    private HuffmanNode root;
    private MinHeap heap;
    private final String[] codes = new String[256];
    private final int[] frequencies;

    public HuffmanTree(int[] frequencies) {
        if (frequencies == null || frequencies.length != 256) {
            throw new IllegalArgumentException("A frequency array with 256 entries is required.");
        }
        if (Arrays.stream(frequencies).anyMatch(value -> value < 0)) {
            throw new IllegalArgumentException("Frequencies cannot be negative.");
        }
        this.frequencies = frequencies.clone();
    }

    public String[] getCodes() {
        return codes;
    }

    public void createHeap() {
        int symbolCount = 0;
        for (int frequency : frequencies) {
            if (frequency > 0) {
                symbolCount++;
            }
        }

        heap = new MinHeap(symbolCount);
        for (int value = 0; value < frequencies.length; value++) {
            if (frequencies[value] > 0) {
                heap.insert(new HuffmanNode(value, frequencies[value]));
            }
        }
    }

    public void huffManTree() {
        if (heap == null) {
            throw new IllegalStateException("Create the heap before building the Huffman tree.");
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

        root = heap.extractMin();
    }

    public void generateCodes() {
        Arrays.fill(codes, null);
        generateCodes(root, "");
    }

    private void generateCodes(HuffmanNode node, String code) {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
            codes[node.getValue()] = code.isEmpty() ? "0" : code;
            return;
        }

        generateCodes(node.getLeft(), code + "0");
        generateCodes(node.getRight(), code + "1");
    }

    public String getCodesAsString() {
        StringBuilder result = new StringBuilder("Huffman Codes\n")
                .append("---------------------------------------------------------\n");

        for (int value = 0; value < codes.length; value++) {
            if (codes[value] != null) {
                result.append(String.format(
                        "Byte %-3d | Frequency: %-6d | Huffman Code: %-15s | Length: %-2d%n",
                        value, frequencies[value], codes[value], codes[value].length()));
            }
        }

        if (root == null) {
            result.append("The selected file is empty.\n");
        }
        result.append("---------------------------------------------------------\n");
        return result.toString();
    }
}
