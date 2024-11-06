
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

class Customer extends Thread implements Comparable<Customer> {

    //Constructor --------------------------------------------------------------------
    private final int id;
    private final LocalTime arrivalTime;
    private LocalTime serveTime;
    private final Order order;
    private final CircularBuffer<Order> orderBuffer;
    private final BoundedQueue<Customer> tableBuffer;
    private final HashMap<String, Integer> meals;
    private int delay;

    public Customer(int id, LocalTime arrivalTime, Order order, CircularBuffer<Order> orderBuffer, BoundedQueue<Customer> tableBuffer, HashMap<String, Integer> meals) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serveTime = arrivalTime;
        this.order = order;
        this.orderBuffer = orderBuffer;
        this.tableBuffer = tableBuffer;
        this.meals = meals;
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
            tableBuffer.add(this);
            timeline.put("Seated", getTimeDifference(operationStart));
            //customer places order
            orderBuffer.add(order);
            timeline.put("Order", getTimeDifference(operationStart));
            //customer waits for order
            timeline.put("ChefStart", getTimeDifference(operationStart));
            int chefId = order.waitUntilOrderReady();
            timeline.put("ChefFinish", getTimeDifference(operationStart));
            timeline.put("Leave", getTimeDifference(operationStart));
            this.serveTime = getTimeDifference(operationStart);
            Customer removed = (Customer) tableBuffer.remove();
            System.out.println("Customer " + removed.id + " has left the restaurant.");
            printEvents(timeline, this.id, chefId, 1);
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

    public void printEvents(HashMap<String, LocalTime> events, int customerId, int chefId, int waiterId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        // Ensure we only print each event once
        if (events.containsKey("Arrival")) {
            System.out.printf("[%s] Customer %d arrives.%n", events.get("Arrival").format(formatter), customerId);
        }
        if (events.containsKey("Seated")) {
            System.out.printf("[%s] Customer %d is seated at Table 1%n", events.get("Seated").format(formatter), customerId);
        }
        if (events.containsKey("Order")) {
            System.out.printf("[%s] Customer %d places an order: Burger%n", events.get("Order").format(formatter), customerId);
        }
        if (events.containsKey("ChefStart")) {
            System.out.printf("[%s] Chef %d starts preparing Burger for Customer %d%n", events.get("ChefStart").format(formatter), chefId, customerId);
        }
        if (events.containsKey("ChefFinish")) {
            System.out.printf("[%s] Chef %d finishes preparing Burger for Customer %d%n", events.get("ChefFinish").format(formatter), chefId, customerId);
        }
        if (events.containsKey("Serve")) {
            System.out.printf("[%s] Waiter %d serves Burger to Customer %d at Table 1%n", events.get("Serve").format(formatter), waiterId, customerId);
        }
        if (events.containsKey("Leave")) {
            System.out.printf("[%s] Customer %d finishes eating and leaves the restaurant.%n", events.get("Leave").format(formatter), customerId);
            System.out.printf("[%s] Table 1 is now available.%n", events.get("Leave").format(formatter));
        }
    }
}
