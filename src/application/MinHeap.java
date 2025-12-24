package application;

// MinHeap implementation used to build the Huffman Tree
public class MinHeap {

    // Array that stores heap nodes (1-based index)
    private HuffmanNode[] heap;

    // Maximum number of elements the heap can store
    private int max_Size;

    // Current number of elements in the heap
    private int capacity;

    // Constructor initializes the heap with given maximum size
    MinHeap(int max_Size) {
        this.max_Size = max_Size;
        heap = new HuffmanNode[max_Size + 1]; // index 0 is unused
        capacity = 0;
    }

    // Returns the heap array
    public HuffmanNode[] getHeap() {
        return heap;
    }

    // Returns maximum heap size
    public int getMax_Size() {
        return max_Size;
    }

    // Returns current number of elements
    public int getCapacity() {
        return capacity;
    }

    // Returns index of left child
    public int leftChild(int i) {
        return 2 * i;
    }

    // Returns index of right child
    public int rightChild(int i) {
        return (2 * i) + 1;
    }

    // Checks if heap is empty
    public boolean isEmpty() {
        return capacity == 0;
    }

    // Swaps two nodes in the heap
    public void swap(int i, int j) {
        HuffmanNode temp = heap[j];
        heap[j] = heap[i];
        heap[i] = temp;
    }

    // Restores the min-heap property starting from index i
    public void minHeapify(int i) {

        int smallest = i;
        int left = leftChild(i);
        int right = rightChild(i);

        // Compare with left child
        if (left <= capacity &&
                (heap[left].getFrequency() < heap[smallest].getFrequency() ||
                        (heap[left].getFrequency() == heap[smallest].getFrequency()
                                && heap[left].getValue() < heap[smallest].getValue()))) {

            smallest = left;
        }

        // Compare with right child
        if (right <= capacity &&
                (heap[right].getFrequency() < heap[smallest].getFrequency() ||
                        (heap[right].getFrequency() == heap[smallest].getFrequency()
                                && heap[right].getValue() < heap[smallest].getValue()))) {

            smallest = right;
        }

        // If the smallest is not the current node, swap and continue heapifying
        if (smallest != i) {
            swap(i, smallest);
            minHeapify(smallest);
        }
    }

    // Builds a valid min heap from the current elements
    public void buildMinHeap() {
        for (int i = capacity / 2; i >= 1; i--) {
            minHeapify(i);
        }
    }

    // Inserts a new node into the heap
    public void insert(HuffmanNode node) {

        // Check if heap is full
        if (capacity >= max_Size) {
            return;
        }

        capacity++;
        int i = capacity;
        heap[i] = node;

        // Move the node up until heap property is satisfied
        while (i > 1 &&
                (heap[i].getFrequency() < heap[i / 2].getFrequency() ||
                        (heap[i].getFrequency() == heap[i / 2].getFrequency()
                                && heap[i].getValue() < heap[i / 2].getValue()))) {

            swap(i, i / 2);
            i = i / 2;
        }
    }

    // Removes and returns the node with minimum frequency
    public HuffmanNode extractMin() {

        // If heap is empty
        if (isEmpty()) {
            return null;
        }

        // The root contains the minimum element
        HuffmanNode min = heap[1];

        // Move last element to root
        heap[1] = heap[capacity];
        capacity--;

        // Restore heap property
        minHeapify(1);

        return min;
    }
}
