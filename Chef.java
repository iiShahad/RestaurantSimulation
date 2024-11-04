
import java.util.HashMap;

class Chef extends Thread {

    private final int id;
    private final CircularBuffer<String> orderBuffer;
    private final HashMap<String, Integer> meals;

    public Chef(int id, CircularBuffer<String> orderBuffer, HashMap<String, Integer> meals) {
        this.id = id;
        this.orderBuffer = orderBuffer;
        this.meals = meals;
    }

    CustomSemaphore mutex = new CustomSemaphore(1);
    boolean endShift = false;

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

                } finally {
                    if (mutex.getPermits() == 0) {
                        mutex.release();
                    }
                }
            } catch (Exception e) {
                System.err.println("Exception in run method: " + e.getMessage());
            }
        }

    }

    public void endShift() {
        System.out.println("Chef " + id + " has ended the shift");
        endShift = true;
    }
}
