
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

class Customer extends Thread implements Comparable<Customer> {

    //Constructor --------------------------------------------------------------------
    private final int id;
    private final LocalTime arrivalTime;
    private LocalTime serveTime;
    private final Order order;
    private final CircularBuffer<Order> orderBuffer;
    private final BoundedQueue<Customer> tableBuffer;
    private final Map<Integer, CustomerData> customerServingTimeline;

    private int delay;

    public Customer(int id, LocalTime arrivalTime, Order order, CircularBuffer<Order> orderBuffer, BoundedQueue<Customer> tableBuffer, Map<Integer, CustomerData> customerServingTimeline) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serveTime = arrivalTime;
        this.order = order;
        this.orderBuffer = orderBuffer;
        this.tableBuffer = tableBuffer;
        this.customerServingTimeline = customerServingTimeline;
    }

    private final HashMap<String, LocalTime> timeline = new HashMap<>();

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
            System.out.println("delay: " + delay);
            Thread.sleep((delay > 0) ? delay * 1000 * 60 : 1);
            System.out.println("Customer " + id + " has arrived.");
            //customer arrives
            timeline.put("Arrival", arrivalTime);
            LocalTime operationStart = LocalTime.now();
            //customer is seated
            System.out.println("Customer " + id + " is seated at Table");
            int tableIndex = tableBuffer.add(this) + 1;

            timeline.put("Seated", getTimeDifference(operationStart));
            //customer places order
            System.out.println("Customer " + id + " places an order: " + order.getMealName());
            orderBuffer.add(order);
            timeline.put("Order", getTimeDifference(operationStart));
            //customer waits for order
            System.out.println("Customer " + id + " is waiting for the order to be ready.");
            timeline.put("ChefStart", getTimeDifference(operationStart));
            int chefId = order.waitUntilOrderReady();
            System.out.println("Customer " + id + " receives the order from Chef " + chefId);
            timeline.put("ChefFinish", getTimeDifference(operationStart));
            System.out.println("Customer " + id + " starts eating.");
            timeline.put("Leave", getTimeDifference(operationStart));
            this.serveTime = getTimeDifference(operationStart);

            CustomerData customerData = new CustomerData(id, chefId, 1, timeline, order, tableIndex);
            customerServingTimeline.put((this.id - 1), customerData);
            Customer removed = (Customer) tableBuffer.remove(tableIndex);
            System.out.println("Customer " + removed.id + " has left the restaurant.");
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

    public Order getOrder() {
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
        return this.serveTime.compareTo(o.serveTime);
    }

    private LocalTime getTimeDifference(LocalTime operationStart) {
        return arrivalTime.plusMinutes(Duration.between(operationStart, LocalTime.now()).toMinutes());
    }

}
