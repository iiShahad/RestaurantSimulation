public class Waiter extends Thread{
        //Constructor --------------------------------------------------------------------
        private final int id;
        private final CircularBuffer<Order> readyOrderBuffer;
    
        public Waiter(int id, CircularBuffer<Order> readyOrderBuffer) {
            this.id = id;
            this.readyOrderBuffer = readyOrderBuffer;
        }
    
        //Global variables --------------------------------------------------------------
        CustomSemaphore mutex = new CustomSemaphore(1);
        boolean endWaiter = false;
    
        //Thread run method -------------------------------------------------------------
        /*
        Steps:
        1. The loop continues until either `endWaiter` is true or the `readyOrderBuffer` is empty.
        - This ensures that the Waiter works until the end of their shift or until all orders have been served.
    
        2. Acquire the `mutex` lock to ensure mutual exclusion when accessing the shared `readyOrderBuffer`.
        - This prevents other threads from modifying the `redyOrderBuffer` concurrently, ensuring thread safety.
    
        3. Check if the `readyOrderBuffer` is empty:
        - If it is empty, release the `mutex` and continue to the next iteration of the loop (waiting for new redyOrders).
    
        4. If the buffer is not empty, remove an order from the `readyOrderBuffer` and store it in `orderMeal`.//////may be changeeeeeeeeee
        - The Chef retrieves an order to prepare, and the order’s details are logged.
    
        5. After releasing the mutex, simulate the time taken to serve the meal by calling `Thread.sleep()` with only one minute.
        - This models the time it takes the Waiter to serve the meal.
    
        6. Once the meal is served, log the completion, and mark the order as served by calling `orderMeal.markOrderServed(this.id)`.
        - This notifies that the meal is ready for the customer.
    
        7. In the `finally` block, check if the `mutex` lock has been acquired and ensure it is released if necessary, avoiding a deadlock.
        */
        @Override
        public void run() {
            while (!endWaiter || !readyOrderBuffer.isEmpty()) { //Continue until the end of the shift or all orders are processed
                try {
                    mutex.acquire(); //Acquire the mutex lock
                    try {
                        if (readyOrderBuffer.isEmpty()) {
                            mutex.release(); 
                            continue; 
                        }
    
                        //retrieve order from ready order buffer
                        Order orderMeal = (Order) readyOrderBuffer.remove();
    
                        System.out.println("Waiter " + id + " is serving " + orderMeal.getMealName() + " for Customer " + orderMeal.getCustomerId());
                        orderMeal.markOrderServing(this.id);
    
                        mutex.release(); //Release the mutex lock
    
                        //simulate meal serving time
                        Thread.sleep(1000 * 1);
                        System.out.println("Waiter " + id + " has served " + orderMeal.getMealName() + " for Customer " + orderMeal.getCustomerId());
    
                        //mark order as served and notify customer
                        orderMeal.markOrderServed();
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
        public void endWaiter() {
            System.out.println("Waiter " + id + " has ended the shift");
            endWaiter = true;
        }
    
    
}
