
import java.time.LocalTime;
import java.time.Duration;
import java.util.Random;

class Customer extends Thread implements Comparable<Customer> {

    private final int id;
    private final LocalTime arrivalTime;
    private final int eatingTime;
    private final String order;
    private final CircularBuffer<String> orderBuffer;
    private  LocalTime currentTime;
    private int delay;

    public Customer(int id, LocalTime arrivalTime, String order, CircularBuffer<String> orderBuffer) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.currentTime = arrivalTime;//current time starts with arrival time and changes gradually
        this.eatingTime = getRandom();
        this.order = order;
        this.orderBuffer = orderBuffer;
    }

    @Override
    public void run() {
        try {
            //FIXME: make this delay in minutes
            Thread.sleep(100 * delay);
            System.out.println("Customer ID: " + id + " Arrival Time: " + arrivalTime + " Order: " + order);
            orderBuffer.add(order);
            System.out.println("buffer: " + orderBuffer.toString());
        } catch (Exception e) {
            System.err.println("Exception in run method: " + e.getMessage());
        }
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getCustomerId() {
        return id;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalTime getCurrentTime(){
        return currentTime;
    }

    public int getEatingTime(){
        return eatingTime;
    }

    public String getOrder() {
        return order;
    }

     public void addToCurrentTime(int minutes){
     currentTime.plus(Duration.ofMinutes(minutes));
     }

    @Override
    public String toString() {
        return "Customer ID: " + id + " Arrival Time: " + arrivalTime + " Order: " + order;
    }

    @Override
    public int compareTo(Customer o) {
        //return the earliest arrival time
        return this.arrivalTime.compareTo(o.arrivalTime);
    }

      public int getRandom(){
          Random random = new Random();
          int min=5;
          int max=15;
          int num=((random.nextInt((max- min) + 1) + min));
          return num;
        
    }
}
