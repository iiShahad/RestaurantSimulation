
import java.time.LocalTime;

class Customer extends Thread implements Comparable<Customer> {
    //Constructor --------------------------------------------------------------------
    private final int id;
    private final LocalTime arrivalTime;
    private final String order;
    private final CircularBuffer<String> orderBuffer;
    private final CircularBuffer<Customer> tableBuffer;
    private int delay;

    public Customer(int id, LocalTime arrivalTime, String order, CircularBuffer<String> orderBuffer, CircularBuffer<Customer> tableBuffer) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.order = order;
        this.orderBuffer = orderBuffer;
        this.tableBuffer = tableBuffer;
    }

    //Thread run method -------------------------------------------------------------
    /*
    What we need to do:
    1. Wait for the delay time to simulate the arrival of the customer
    2. Add the order to the orderBuffer
    3. Add the customer to the tableBuffer
    */
    @Override
    public void run() {
        try {
            //FIXME: make this delay in minutes
            Thread.sleep(1000 * delay);
            System.out.println("Customer ID: " + id + " Arrival Time: " + arrivalTime + " Order: " + order);
            orderBuffer.add(order);
            tableBuffer.add(this);
            System.out.println("buffer: " + orderBuffer.toString());
        } catch (Exception e) {
            System.err.println("Exception in run method: " + e.getMessage());
        }
    }

    //getters and setters ----------------------------------------------------------
    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getCustomerId() {
        return id;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public String getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "Customer ID: " + id + " Arrival Time: " + arrivalTime + " Order: " + order;
    }

    //compareTo method -------------------------------------------------------------
    //This method is used to sort the customers by arrival time in the priority queue (customerArrivalQueue)
    @Override
    public int compareTo(Customer o) {
        //return the earliest arrival time, because the this.arrivalTime comes first, it will be sorted in ascending order
        return this.arrivalTime.compareTo(o.arrivalTime);
    }
}
