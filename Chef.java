
import java.util.HashMap;

class Chef extends Thread {
    //Constructor --------------------------------------------------------------------
    private final int id;
    private final CircularBuffer<String> orderBuffer;
    private final CircularBuffer<Customer> tableBuffer;
    private final HashMap<String, Integer> meals;

    public Chef(int id, CircularBuffer<String> orderBuffer, CircularBuffer<Customer> tableBuffer,  HashMap<String, Integer> meals) {
        this.id = id;
        this.orderBuffer = orderBuffer;
        this.meals = meals;
        this.tableBuffer = tableBuffer;
    }

    //Global variables --------------------------------------------------------------
    CustomSemaphore mutex = new CustomSemaphore(1);
    boolean endShift = false;

    //Thread run method -------------------------------------------------------------
    /*
    What we need to do:
    1. Check if the orderBuffer is empty or the shift has ended in a loop to keep the chef waiting for orders
    2. mutex.acquire() to lock the mutex before accessing the orderBuffer
    3. Remove the first order from the orderBuffer
    4. Get the preparing time of the meal from the meals HashMap
    5. mutex.release() to unlock the mutex after accessing the orderBuffer
    6. Simulate the preparation time of the meal by sleeping for preparingTime seconds
    */
    @Override
    public void run() {
        while (!endShift || !orderBuffer.isEmpty()) {
            try {
                mutex.acquire();
                try {
                    if (orderBuffer.isEmpty()) {
                        mutex.release();
                        continue;
                    }
                    String orderMeal = (String) orderBuffer.remove();
                    int preparingTime = meals.get(orderMeal);
                    System.out.println("Chef " + id + " is preparing " + orderMeal + " for " + preparingTime + " minutes");
                    mutex.release();
                    //FIXME: make this delay in minutes
                    // For real minutes, use: preparingTime * 60 * 1000
                    Thread.sleep(1000 * preparingTime);
                    System.out.println("Chef " + id + " has prepared " + orderMeal);
                    //TODO: remove customer from tableBuffer

                } finally {
                    if (mutex.getCurrentPermits() == 0) {
                        mutex.release();
                    }
                }
            } catch (Exception e) {
                System.err.println("Exception in run method: " + e.getMessage());
            }
        }

    }

    //endShift method --------------------------------------------------------------
    //This method is used to end the shift of the chef when all the customers have been served
    public void endShift() {
        System.out.println("Chef " + id + " has ended the shift");
        endShift = true;
    }
}
