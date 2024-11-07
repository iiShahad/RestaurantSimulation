class Chef extends Thread {
    //Constructor --------------------------------------------------------------------
    private final int id;
    private final CircularBuffer<Order> orderBuffer;

    public Chef(int id, CircularBuffer<Order> orderBuffer) {
        this.id = id;
        this.orderBuffer = orderBuffer;
    }

    //Global variables --------------------------------------------------------------
    CustomSemaphore mutex = new CustomSemaphore(1);
    boolean endShift = false;

    //Thread run method -------------------------------------------------------------
    /*
    Steps:
    1. The loop continues until either `endShift` is true or the `orderBuffer` is empty.
    - This ensures that the Chef works until the end of their shift or until all orders have been processed.

    2. Acquire the `mutex` lock to ensure mutual exclusion when accessing the shared `orderBuffer`.
    - This prevents other threads from modifying the `orderBuffer` concurrently, ensuring thread safety.

    3. Check if the `orderBuffer` is empty:
    - If it is empty, release the `mutex` and continue to the next iteration of the loop (waiting for new orders).

    4. If the buffer is not empty, remove an order from the `orderBuffer` and store it in `orderMeal`.
    - The Chef retrieves an order to prepare, and the order’s details are logged.

    5. After releasing the mutex, simulate the time taken to prepare the meal by calling `Thread.sleep()` with the meal’s preparation time (in minutes).
    - This models the time it takes the Chef to cook the meal.

    6. Once the meal is prepared, log the completion, and mark the order as ready by calling `orderMeal.markOrderReady(this.id)`.
    - This notifies that the meal is ready for the customer.

    7. In the `finally` block, check if the `mutex` lock has been acquired and ensure it is released if necessary, avoiding a deadlock.
    */
    @Override
    public void run() {
        while (!endShift || !orderBuffer.isEmpty()) { //Continue until the end of the shift or all orders are processed
            try {
                mutex.acquire(); //Acquire the mutex lock
                try {
                    if (orderBuffer.isEmpty()) {
                        mutex.release(); 
                        continue; 
                    }

                    //retrieve order from buffer
                    Order orderMeal = (Order) orderBuffer.remove();

                    System.out.println("Chef " + id + " is preparing " + orderMeal.getMealName() + " for Customer " + orderMeal.getCustomerId());
                    orderMeal.markOrderStart(this.id);

                    mutex.release(); //Release the mutex lock

                    //simulate meal preparation time
                    Thread.sleep(1000 * orderMeal.getMealTime() * 60);
                    System.out.println("Chef " + id + " has prepared " + orderMeal.getMealName() + " for Customer " + orderMeal.getCustomerId());

                    //mark order as ready and notify customer
                    orderMeal.markOrderReady();
                } finally {
                    //Ensure mutex is released in case of exception
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
