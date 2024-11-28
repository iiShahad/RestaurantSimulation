
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class BoundedQueue<T> {
    private final int maxSize;
    private final PriorityQueue<T> queue = new PriorityQueue<>();
    private final int[] indices;
    private final CustomSemaphore consumingSemaphore;
    private final CustomSemaphore producingSemaphore;

    public BoundedQueue(int maxSize) {
        this.maxSize = maxSize;
        producingSemaphore = new CustomSemaphore(maxSize);
        consumingSemaphore = new CustomSemaphore(0);
        indices = new int[maxSize];
    }

    //Global variables --------------------------------------------------------------
    private final Lock lock = new ReentrantLock();

    public int add(T item) {
        int index = -1;
        try {
            producingSemaphore.acquire(); // Wait for space in the queue
            lock.lock(); // Lock the queue
            try {
                index = findFirstEmptyIndex();
                indices[index] = 1;
                queue.offer(item); // Add the item to the queue
            } finally {
                lock.unlock(); // Unlock the queue
            }
            consumingSemaphore.release(); // Signal that an item is available to consume
        } catch (Exception e) {
            System.err.println("Exception in queue add method: " + e.getMessage());
        }
        return index;
    }

    public Object remove(int tableIndex) {
        Object item = null;
        try {
            consumingSemaphore.acquire(); // Wait for an item to be available
            lock.lock(); // Lock the queue
            try {
                item = queue.poll(); // Remove and return the highest-priority item
                indices[tableIndex] = 0;
            } finally {
                lock.unlock(); // Unlock the queue
            }
            producingSemaphore.release(); // Signal that there is space in the queue
        } catch (Exception e) {
            System.err.println("Exception in queue remove method: " + e.getMessage());
        }
        return item;
    }


    private int findFirstEmptyIndex() {
        for (int i = 0; i < indices.length; i++) {
            if (indices[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    //getters --------------------------------------------------------------------
    public boolean isFull() {
        return queue.size() == maxSize;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }

    @Override
    public String toString() {
        return queue.toString();
    }
}
