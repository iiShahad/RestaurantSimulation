
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class CustomSemaphore {
    //Constructor --------------------------------------------------------------------
    private final int maxPermits; //maxPermits is the total number of permits should be available
    private int permits; //permits is the number of permits available

    public CustomSemaphore(int maxPermits) {
        this.maxPermits = maxPermits;
        this.permits = maxPermits;
    }

    //Global variables --------------------------------------------------------------
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    //acquire method --------------------------------------------------------------
    //This method is used to acquire a permit from the semaphore
    public void acquire() {
        //thread will lock the lock when it enters the method
        lock.lock();
        try {
            //if permits are 0, thread will wait until it is notified if there are any permits available
            while (permits == 0) {
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

    //release method --------------------------------------------------------------
    //This method is used to release a permit to the semaphore
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

    //getters --------------------------------------------------------------------
    public int getCurrentPermits() {
        return permits;
    }
}
