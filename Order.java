
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Order {

    //Constructor --------------------------------------------------------------------
    private final String mealName;
    private final int mealTime;
    private final int customerId;
    private int chefId;

    public Order(String mealName, int mealTime, int customerId) {
        this.mealName = mealName;
        this.mealTime = mealTime;
        this.customerId = customerId;
    }

    //Global variables --------------------------------------------------------------
    private final Lock lock = new ReentrantLock();
    private final Condition orderReadyCondition = lock.newCondition();
    private boolean orderReady = false;

    //waitUntilOrderReady method ----------------------------------------------------
    /*
    This method is used to make a thread wait until the order is ready for the customer. It ensures proper synchronization and avoids busy-waiting using the `Condition` variable.

    Steps:
    1. Lock the object using `lock.lock()` to ensure mutual exclusion when checking and modifying the `orderReady` condition.
    - This prevents other threads from changing the order state or notifying prematurely.

    2. Enter a `while` loop that checks if the order is not ready (`!orderReady`):
    - If the order is not ready, the thread will wait for the `orderReadyCondition` to be signaled.
    - The `await()` method makes the current thread wait until it is notified (signaled) by another thread (e.g., the chef when the order is ready).

    3. If the order is ready, the thread will proceed once it is notified and the `while` condition is no longer true.

    4. Catch any exceptions that occur during the waiting process and log them for debugging purposes.

    5. Finally, release the lock using `lock.unlock()` to allow other threads to access the shared resource.

    Returns:
    - The method returns the `chefId`, which represents the chef who prepared the order. This can be used by the customer to know which chef completed their meal.
    */
    public int waitUntilOrderReady() {
        lock.lock();
        try {
            while (!orderReady) {
                orderReadyCondition.await(); //thread will keep waiting until order is ready
            }
        } catch (Exception e) {
            System.err.println("Exception in waitUntilOrderReady: " + e.getMessage());
        } finally {
            lock.unlock();
        }
        return chefId;
    }

    //markOrderReady method ---------------------------------------------------------
    /*
    This method is used to mark an order as ready and notify the waiting customer that the order can now be served. It updates the order status and signals the waiting thread (the customer) that the order is ready.

    Steps:
    1. Lock the object using `lock.lock()` to ensure mutual exclusion when modifying shared variables (`orderReady` and `chefId`).
    - This prevents other threads from changing the state of the order or notifying prematurely.

    2. Set the `orderReady` flag to `true`, indicating that the order is ready to be served.

    3. Assign the `chefId` to the current chef's ID, indicating which chef prepared the order. This information will be used by the customer to know which chef completed their meal.

    4. Signal the waiting customer (or thread) by calling `orderReadyCondition.signal()`:
    - This releases a thread that is waiting on the `orderReadyCondition`, allowing the customer to proceed and receive their order.

    5. Catch any exceptions that occur during the marking process and log them for debugging purposes.

    6. Finally, release the lock using `lock.unlock()` to allow other threads to access the shared resource.
     */
    public void markOrderReady(int chefId) {
        lock.lock();
        try {
            orderReady = true;
            this.chefId = chefId;
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
