
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class CircularBuffer<T> {

    private final int maxSize;
    private final T[] buffer;
    private final CustomSemaphore consumingSemaphore;
    private final CustomSemaphore producingSemaphore;

    public CircularBuffer(int maxSize) {
        this.maxSize = maxSize;
        buffer = (T[]) new Object[maxSize];
        consumingSemaphore = new CustomSemaphore(maxSize);
        producingSemaphore = new CustomSemaphore(0);
    }

    private int head = 0;
    private int tail = 0;

    private final Lock lock = new ReentrantLock();

    public boolean isFull() {
        return head == tail && buffer[head] != null;
    }

    public void add(T item) {
        lock.lock();
        try {
            producingSemaphore.acquire();
            buffer[head] = item;
            head = (head + 1) % maxSize;
            consumingSemaphore.release();
        } catch (Exception e) {
            System.err.println("Exception in add method: " + e.getMessage());
        } finally {
            lock.unlock();
        }

    }

    public T remove() {
        lock.lock();
        T item = null;
        try {
            consumingSemaphore.acquire();
            item = buffer[tail];
            buffer[tail] = null;
            tail = (tail + 1) % maxSize;
            producingSemaphore.release();
        } catch (Exception e) {
            System.err.println("Exception in remove method: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return item;
    }

}
