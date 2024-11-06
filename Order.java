
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Order {

    //Constructor --------------------------------------------------------------------
    private String mealName;
    private int mealTime;
    private int customerId;

    public Order(String mealName, int mealTime, int customerId) {
        this.mealName = mealName;
        this.mealTime = mealTime;
        this.customerId = customerId;
    }
    private final Lock lock = new ReentrantLock();
    private final Condition orderReadyCondition = lock.newCondition();
    private boolean orderReady = false;

    public void waitUntilOrderReady() {
        lock.lock();
        try {
            while (!orderReady) {
                orderReadyCondition.await();
            }
        } catch (Exception e) {
            System.err.println("Exception in waitUntilOrderReady: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    public void markOrderReady() {
        lock.lock();
        try {
            orderReady = true;
            orderReadyCondition.signal();
        } catch (Exception e) {
            System.err.println("Exception in markOrderReady: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    //getters ------------------------------------------------------------------------
    public String getMealName() {
        return mealName;
    }

    public int getMealTime() {
        return mealTime;
    }

    public int getCustomerId() {
        return customerId;
    }

    @Override
    public String toString() {
        return "Meal Name: " + mealName + " Meal Time: " + mealTime + " Customer ID: " + customerId;
    }
}
