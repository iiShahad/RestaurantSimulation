
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CircularBuffer<T> {
    private final int maxSize;
    private final Object[] buffer;
    private final CustomSemaphore consumingSemaphore;
    private final CustomSemaphore producingSemaphore;
    private volatile boolean endShift = false;

    public CircularBuffer(int maxSize) {
        this.maxSize = maxSize;
        buffer = new Object[maxSize];
        producingSemaphore = new CustomSemaphore(maxSize);
        consumingSemaphore = new CustomSemaphore(0);
    }

    //Global variables --------------------------------------------------------------
    private int head = 0;
    private int tail = 0;
    private int size = 0;

    private final Lock lock = new ReentrantLock();

  
    public void add(T item) {
        try {
            producingSemaphore.acquire();
            lock.lock();
            try {
                if (endShift) {
                    return;
                }
                buffer[head] = item;
                head = (head + 1) % maxSize;
                size++;
            } finally {
                lock.unlock();
            }
            consumingSemaphore.release();
        } catch (Exception e) {
            System.err.println("Exception in add method: " + e.getMessage());
        }

    }

    public Object remove() {
        Object item = null;
        try {
            consumingSemaphore.acquire();
            lock.lock();
            try {
                if (size > 0) {
                    item = buffer[tail];
                    tail = (tail + 1) % maxSize;
                    size--;
                }
            } finally {
                lock.unlock();
            }
            producingSemaphore.release();
        } catch (Exception e) {
            System.err.println("Exception in remove method: " + e.getMessage());

        }
        return item;
    }

    // Method to end the shift and release semaphores
    // the remove method will return null if the buffer is empty and the thread will exit when receiving a null value
    // the add method will not add any new items to the buffer if the shift has ended
    public void endShift() {
        endShift = true;
        consumingSemaphore.release();
        producingSemaphore.release(); 
    }

    //getters --------------------------------------------------------------------
    public boolean isFull() {
        return size == maxSize;
    }

    public boolean isEmpty() {
        return size == 0;
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
