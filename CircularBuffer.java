
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CircularBuffer<T> {
    //Constructor --------------------------------------------------------------------
    private final int maxSize;
    private final Object[] buffer;
    private final CustomSemaphore consumingSemaphore;
    private final CustomSemaphore producingSemaphore;

    public CircularBuffer(int maxSize) {
        this.maxSize = maxSize;
        buffer = new Object[maxSize];
        producingSemaphore = new CustomSemaphore(maxSize);
        consumingSemaphore = new CustomSemaphore(0);
    }

    //Global variables --------------------------------------------------------------
    private int head = 0;
    private int tail = 0;

    private final Lock lock = new ReentrantLock();


    //add method --------------------------------------------------------------------
    /*
    This method is used to add an item to the circular buffer while ensuring thread safety and proper synchronization using semaphores.

    Steps:
    1. Acquire a permit from the `producingSemaphore` to ensure that there is space available in the buffer.
    - This prevents producing items when the buffer is full, managing the number of items being added.

    2. Lock the buffer to ensure that only one thread can modify the buffer at a time.
    - The `lock` ensures mutual exclusion when accessing the buffer, preventing race conditions.

    3. Add the item to the buffer:
    - Store the item at the `head` index in the `buffer` array.
    - Update the `head` index to point to the next available position using modulo arithmetic to ensure the circular behavior (`head = (head + 1) % maxSize`).

    4. Release the lock after the item is added to the buffer.
    - Releasing the lock allows other threads to access and modify the buffer.

    5. Release a permit to the `consumingSemaphore` to signal that there is an item available for consumption.
    - This allows consumer threads to proceed when there is at least one item in the buffer.

    If an exception occurs during any of these steps, it is caught and logged.
    */
    public void add(T item) {
        try {
            producingSemaphore.acquire();
            lock.lock();
            try {
                buffer[head] = item;
                head = (head + 1) % maxSize;
            } finally {
                lock.unlock();
            }
            consumingSemaphore.release();
        } catch (Exception e) {
            System.err.println("Exception in add method: " + e.getMessage());
        }

    }

    //remove method --------------------------------------------------------------------
    /*
    This method is used to remove an item from the circular buffer while ensuring thread safety and proper synchronization using semaphores.

    Steps:
    1. Acquire a permit from the `consumingSemaphore` to ensure that there is at least one item available in the buffer for consumption.
    - This prevents consumers from trying to remove items when the buffer is empty.

    2. Lock the buffer to ensure that only one thread can access and modify the buffer at a time.
    - The `lock` ensures mutual exclusion, preventing race conditions when removing items.

    3. Remove the item from the buffer:
    - Retrieve the item from the `tail` index in the `buffer` array.
    - Set the position at the `tail` index to `null` to indicate that the item has been removed.
    - Update the `tail` index to point to the next available position using modulo arithmetic (`tail = (tail + 1) % maxSize`) to ensure circular behavior.

    4. Release the lock after the item is removed from the buffer.
    - This allows other threads to access and modify the buffer.

    5. Release a permit to the `producingSemaphore` to signal that there is space available in the buffer for new items.
    - This allows producer threads to proceed when there is space to add new items.
    */
    public Object remove() {
        Object item = null;
        try {
            consumingSemaphore.acquire();
            lock.lock();
            try {
                item = buffer[tail];
                buffer[tail] = null;
                tail = (tail + 1) % maxSize;
            } finally {
                lock.unlock();
            }
            producingSemaphore.release();

        } catch (Exception e) {
            System.err.println("Exception in remove method: " + e.getMessage());

        }
        return item;
    }

    //getters --------------------------------------------------------------------
    public boolean isFull() {
        return head == tail && buffer[head] != null;
    }

    public boolean isEmpty() {
        return head == tail && buffer[head] == null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Object item : buffer) {
            if (item != null) {
                sb.append(item).append(" ");
            }
        }
        return sb.toString();
    }
}
