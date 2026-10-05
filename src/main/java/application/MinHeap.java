package application;

public class MinHeap {
    private final HuffmanNode[] heap;
    private final int maxSize;
    private int capacity;

    MinHeap(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("Heap size cannot be negative.");
        }
        this.maxSize = maxSize;
        heap = new HuffmanNode[maxSize + 1];
    }

    public int getCapacity() {
        return capacity;
    }

    public void insert(HuffmanNode node) {
        if (node == null) {
            throw new IllegalArgumentException("Heap nodes cannot be null.");
        }
        if (capacity >= maxSize) {
            throw new IllegalStateException("The heap is full.");
        }

        capacity++;
        int index = capacity;
        heap[index] = node;

        while (index > 1 && comesBefore(heap[index], heap[index / 2])) {
            swap(index, index / 2);
            index /= 2;
        }
    }

    public HuffmanNode extractMin() {
        if (capacity == 0) {
            return null;
        }

        HuffmanNode minimum = heap[1];
        heap[1] = heap[capacity];
        heap[capacity] = null;
        capacity--;

        if (capacity > 0) {
            minHeapify(1);
        }
        return minimum;
    }

    private void minHeapify(int index) {
        int smallest = index;
        int left = index * 2;
        int right = left + 1;

        if (left <= capacity && comesBefore(heap[left], heap[smallest])) {
            smallest = left;
        }
        if (right <= capacity && comesBefore(heap[right], heap[smallest])) {
            smallest = right;
        }
        if (smallest != index) {
            swap(index, smallest);
            minHeapify(smallest);
        }
    }

    private boolean comesBefore(HuffmanNode first, HuffmanNode second) {
        return first.getFrequency() < second.getFrequency()
                || (first.getFrequency() == second.getFrequency()
                        && first.getValue() < second.getValue());
    }

    private void swap(int first, int second) {
        HuffmanNode temporary = heap[first];
        heap[first] = heap[second];
        heap[second] = temporary;
    }
}
