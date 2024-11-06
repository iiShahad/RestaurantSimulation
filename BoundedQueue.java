
import java.util.PriorityQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class BoundedQueue<T>{
     //Constructor --------------------------------------------------------------------
    private final int maxSize;
    private final PriorityQueue<T> queue = new PriorityQueue<>();
    private final CustomSemaphore consumingSemaphore;
    private final CustomSemaphore producingSemaphore;

    public BoundedQueue(int maxSize) {
        this.maxSize = maxSize;
        producingSemaphore = new CustomSemaphore(maxSize);
        consumingSemaphore = new CustomSemaphore(0);
    }

    //Global variables --------------------------------------------------------------
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
            producingSemaphore.acquire(); // Wait for space in the queue
            lock.lock(); // Lock the queue
            try {
                queue.offer(item); // Add the item to the queue
            } finally {
                lock.unlock(); // Unlock the queue
            }
            consumingSemaphore.release(); // Signal that an item is available to consume
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
            consumingSemaphore.acquire(); // Wait for an item to be available
            lock.lock(); // Lock the queue
            try {
                item = queue.poll(); // Remove and return the highest-priority item
            } finally {
                lock.unlock(); // Unlock the queue
            }
            producingSemaphore.release(); // Signal that there is space in the queue
        } catch (Exception e) {
            System.err.println("Exception in remove method: " + e.getMessage());
        }
        return item;
    }

    //getters --------------------------------------------------------------------
    public boolean isFull() {
        return queue.size() == maxSize;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public String toString() {
        return queue.toString();
    }
}