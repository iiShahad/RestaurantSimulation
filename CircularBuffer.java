
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CircularBuffer<T> {

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

    private int head = 0;
    private int tail = 0;

    private final Lock lock = new ReentrantLock();

    public boolean isFull() {
        return head == tail && buffer[head] != null;
    }

    public boolean isEmpty() {
        return head == tail && buffer[head] == null;
    }

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
