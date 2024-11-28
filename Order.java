
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Order {
    //Constructor --------------------------------------------------------------------
    private final String mealName;
    private final int mealTime;
    private final int customerId;
    private int chefId;
    private int waiterId;

    public Order(String mealName, int mealTime, int customerId) {
        this.mealName = mealName;
        this.mealTime = mealTime;
        this.customerId = customerId;
    }

    //Global variables --------------------------------------------------------------
    private final Lock lock = new ReentrantLock();
    private final Condition orderReadyCondition = lock.newCondition();
    private final Condition orderStartedCondition = lock.newCondition(); 
    private final Condition orderServedCondition = lock.newCondition(); 
    private boolean orderReady = false;
    private boolean orderStart = false;
    private boolean orderServed = false;

    //This method is used by the customer to wait until the order is ready by the chef
    public void waitUntilOrderReady() {
        lock.lock();
        try {
            while (!orderReady) { //while loop to prevent spurious wakeup
                orderReadyCondition.await();
            }
        } catch (Exception e) {
            System.err.println("Exception in waitUntilOrderReady: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    //This method is used by the customer to wait until the order is started by the chef
    public int waitUntilOrderStart() {
        lock.lock();
        try {
            while (!orderStart) {
                orderStartedCondition.await(); 
            }
        } catch (Exception e) {
            System.err.println("Exception in waitUntilOrderStart: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return chefId;
    }

    //This method is used by the customer to wait until the order is served by the waiter
    public int waitUntilOrderServed() {
        lock.lock();
        try {
            while (!orderServed) {
                orderServedCondition.await(); 
            }
        } catch (Exception e) {
            System.err.println("Exception in waitUntilOrderServed: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return waiterId;
    }

    //This method is used to mark the order as ready by the chef
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

    //This method is used to mark the order as started by the chef
    public void markOrderStart(int chefId) {
        lock.lock();
        try {
            orderStart = true;
            this.chefId = chefId;
            orderStartedCondition.signal();
        } catch (Exception e) {
            System.err.println("Exception in markOrderReady: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }

    //This method is used to mark the order as served by the waiter
    public void markOrderServed(int waiterId) {
        lock.lock();
        try {
            orderServed = true;
            this.waiterId = waiterId;
            orderServedCondition.signal();
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
