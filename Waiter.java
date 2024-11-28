
import java.util.Random;

class Waiter extends Thread {

    //Constructor --------------------------------------------------------------------
    private final int id;
    private final CircularBuffer<Order> readyOrders;

    public Waiter(int id, CircularBuffer<Order> readyOrders) {
        this.id = id;
        this.readyOrders = readyOrders;
    }

    //Global variables --------------------------------------------------------------
    static CustomSemaphore mutex = new CustomSemaphore(1);
    boolean endShift = false;

    @Override
    public void run() {
        while (!endShift || !readyOrders.isEmpty()) { //Continue until the end of the shift or all orders are processed
            try {
                try {
                    mutex.acquire(); 
                    //retrieve order from buffer
                    Order orderMeal = (Order) readyOrders.remove();
                    if (orderMeal == null) {
                        mutex.release();
                        break;
                    }

                    System.out.println("Waiter " + id + " is serving " + orderMeal.getMealName() + " for Customer " + orderMeal.getCustomerId());

                    mutex.release(); 

                    //simulate meal serving time
                    Thread.sleep((long) (1000 * generateRandomServingTime() * 60 * 0.1));
                    System.out.println("Waiter " + id + " has served " + orderMeal.getMealName() + " for Customer " + orderMeal.getCustomerId());

                    //mark order as ready and notify customer
                    orderMeal.markOrderServed(this.id);
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

    //Helper methods --------------------------------------------------------------
    //This method is used to end the shift of the waiter when all the customers have been served
    public void endShift() {
        System.out.println("Waiter " + id + " has ended the shift");
        endShift = true;
        readyOrders.endShift();
    }

    //This method is used to generate a random serving time time between 5 and 10 minutes
    public int generateRandomServingTime() {
        Random random = new Random();
        int min = 5;
        int max = 10;
        int num = ((random.nextInt((max - min) + 1) + min));
        return num;
    }

    static void resetSemaphore() {
        mutex = new CustomSemaphore(1);
    }
}
