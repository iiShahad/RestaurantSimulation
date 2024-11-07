
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class BoundedQueue<T> {
    //Constructor --------------------------------------------------------------------

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

    //add method --------------------------------------------------------------------
    /*
    This method is used to add an item to the queue while managing access with semaphores and ensuring thread-safe operations.

    Steps:
    1. Acquire a permit from `producingSemaphore` to ensure that the queue does not exceed its capacity.
    - `producingSemaphore` limits the total number of items that can be added, preventing overflow.

    2. Lock the queue to maintain thread safety.
    - This lock ensures exclusive access to the queue while adding an item, preventing race conditions.

    3. Add the item to the queue:
    - Identify the first available index in the queue array and mark it as occupied.
    - Use `queue.offer(item)` to enqueue the item, placing it at the end of the queue.

    4. Unlock the queue after adding the item.
    - Releasing the lock allows other threads to access the queue.

    5. Release a permit to `consumingSemaphore` to signal that an item is available for consumption.
    - `consumingSemaphore` tracks available items, allowing consumer threads to access the queue when there are items to consume.

    If an exception occurs during any of these steps, it is caught and logged.
    Returns:
    - The index where the item was added, or -1 if unsuccessful.
     */
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
            System.err.println("Exception in add method: " + e.getMessage());
        }
        return index;
    }

    //remove method --------------------------------------------------------------------
    /*
    This method is used to remove an item from the queue while managing access with semaphores and ensuring thread-safe operations.

    Steps:
    1. Acquire a permit from `consumingSemaphore` to ensure that there is an item available to consume.
    - `consumingSemaphore` prevents the removal of items when the queue is empty, ensuring that consumer threads only proceed when there are items to consume.

    2. Lock the queue to maintain thread safety.
    - The lock ensures that only one thread can remove an item from the queue at a time, preventing race conditions.

    3. Remove the item from the queue:
    - Use `queue.poll()` to remove and return the highest-priority item from the queue (FIFO order).
    - Mark the corresponding index as empty by setting `indices[tableIndex] = 0`.

    4. Unlock the queue after removing the item.
    - Releasing the lock allows other threads to access the queue.

    5. Release a permit to `producingSemaphore` to signal that there is space in the queue.
    - `producingSemaphore` manages the buffer's capacity, allowing producer threads to add items to the queue when space is available.

    If an exception occurs during any of these steps, it is caught and logged.
    Returns:
    - The removed item, or `null` if the removal was unsuccessful.
     */
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
            System.err.println("Exception in remove method: " + e.getMessage());
        }
        return item;
    }

    //findFirstEmptyIndex method -------------------------------------------------------
    /*
    This method is used to find the first index in the `indices` array that is marked as empty (0), indicating that it is available for use.

    Steps:
    1. Iterate over the `indices` array to find the first index where the value is 0.
    - The `indices` array is used to track the status of each position. A value of 1 means the index is occupied, and 0 means it is empty.

    2. If an empty index (value 0) is found, return the index.
    - This index will be used to mark the next available position as occupied.

    3. If no empty index is found (i.e., all positions are occupied), return -1.
    - This indicates that no available positions are left to mark as empty.

    Returns:
    - The index of the first empty position (value 0) in the `indices` array, or -1 if no empty positions are found.
     */
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
