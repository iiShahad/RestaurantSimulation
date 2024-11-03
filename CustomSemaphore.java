
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CustomSemaphore {

    //maxPermits is the total number of permits should be available
    private final int maxPermits;
    //permits is the number of permits available
    private int permits;

    public CustomSemaphore(int maxPermits) {
        this.maxPermits = maxPermits;
        this.permits = maxPermits;
    }

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public void acquire() {
        //thread will lock the lock when it enters the method
        lock.lock();
        try {
            //if permits are 0, thread will wait until it is notified if there are any permits available
            if (permits == 0) {
                condition.await();
            }
            //decrement the number of permits when there is a permit available
            permits--;
        } catch (InterruptedException e) {
            System.err.println("InterruptedException in acquire method: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void release() {
        //thread will lock the lock when it enters the method
        lock.lock();
        try {
            //increment the number of permits
            permits++;
            //notify the waiting threads that there is a permit available
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
}
