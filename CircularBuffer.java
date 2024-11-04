
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
    //This method is used to add an item to the buffer
    /*
    What we need to do:
    1. Wait for a permit from the producingSemaphore, which is used to control the number of items in the buffer
    2. Lock the buffer
    3. Add the item to the buffer
    4. Unlock the buffer
    5. Release a permit to the consumingSemaphore, which is used to control the number of items removed from the buffer
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
    //This method is used to remove the first item from the buffer and return it
    /*
    What we need to do:
    1. Wait for a permit from the consumingSemaphore, which is used to control the number of items removed from the buffer
    2. Lock the buffer
    3. Remove the first item from the buffer
    4. Unlock the buffer
    5. Release a permit to the producingSemaphore, which is used to control the number of items in the buffer
    6. Return the removed item
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
