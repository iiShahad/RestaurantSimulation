

class Chef extends Thread {
    //Constructor --------------------------------------------------------------------
    private final int id;
    private final CircularBuffer<Order> orderBuffer;
    private final CircularBuffer<Customer> tableBuffer;

    public Chef(int id, CircularBuffer<Order> orderBuffer, CircularBuffer<Customer> tableBuffer) {
        this.id = id;
        this.orderBuffer = orderBuffer;
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
                    Order orderMeal = (Order) orderBuffer.remove();
                    
                    System.out.println("Chef " + id + " is preparing " + orderMeal + " for " + orderMeal.getMealTime() + " minutes");
                    mutex.release();
                    //FIXME: make this delay in minutes
                    // For real minutes, use: preparingTime * 60 * 1000
                    Thread.sleep(1000 * orderMeal.getMealTime());
                    System.out.println("Chef " + id + " has prepared " + orderMeal);
                    orderMeal.markOrderReady();
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
