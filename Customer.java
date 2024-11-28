
import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Customer extends Thread implements Comparable<Customer> {
    private final int id;
    private final LocalTime arrivalTime;
    private LocalTime serveTime;
    private final Order order;
    private final CircularBuffer<Order> orderBuffer;
    private final BoundedQueue<Customer> tableQueue;
    private final Map<Integer, CustomerData> customerServingData;

    private int delay;

    public Customer(int id, LocalTime arrivalTime, Order order, CircularBuffer<Order> orderBuffer, BoundedQueue<Customer> tableQueue, Map<Integer, CustomerData> customerServingData) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serveTime = arrivalTime;
        this.order = order;
        this.orderBuffer = orderBuffer;
        this.tableQueue = tableQueue;
        this.customerServingData = customerServingData;
    }

    private final HashMap<String, LocalTime> timeline = new HashMap<>();

    @Override
    public void run() {
        try {
            System.out.println("delay: " + delay);
            Thread.sleep((long)( (delay > 0) ? delay * 1000 * 60 * 0.1 : 1));
            System.out.println("Customer " + id + " has arrived.");

            //customer arrives
            LocalTime operationStart = LocalTime.now(); //store the start time of the operation to calculate the time difference
            timeline.put("Arrival", arrivalTime); //add the arrival time to the timeline

            //customer is seated
            System.out.println("Customer " + id + " is seated at Table");
            int tableIndex = tableQueue.add(this) + 1; //add the customer to the tableQueue and get the table index
            timeline.put("Seated", getTimeDifference(operationStart)); //add the seated time to the timeline

            //customer places order
            System.out.println("Customer " + id + " places an order: " + order.getMealName());
            orderBuffer.add(order); //add the order to the orderBuffer
            timeline.put("Order", getTimeDifference(operationStart)); //add the order time to the timeline

            //customer waits for order
            System.out.println("Customer " + id + " is waiting for the order to be ready.");
            int chefId = order.waitUntilOrderStart();
            timeline.put("ChefStart", getTimeDifference(operationStart)); //add the chef start time to the timeline

            //customer order ready
            order.waitUntilOrderReady(); //wait until the order is ready and get the chef id, the chef will notify the customer when the order is ready
            System.out.println("Customer " + id + " order has been prepared by chef " + chefId);
            timeline.put("ChefFinish", getTimeDifference(operationStart)); //add the chef finish time to the timeline

            //customer receives the order
            int waiterId = order.waitUntilOrderServed(); //wait until the order is served by the waiter
            System.out.println("Customer " + id + " receives the order from the waiter " + waiterId);
            timeline.put("Serve", getTimeDifference(operationStart)); //add the serve time to the timeline

            //customer starts eating
            System.out.println("Customer " + id + " starts eating.");
            Thread.sleep((long)(generateRandomEatingTime() * 1000 * 60 * 0.1)); //simulate the eating time

            //customer finishes eating and leaves
            timeline.put("Leave", getTimeDifference(operationStart)); //add the leave time to the timeline
            this.serveTime = getTimeDifference(operationStart); //store the serve time to sort the customers by serving time in the queue and remove the customer from the tableQueue

            //remove the customer from the tableQueue
            CustomerData customerData = new CustomerData(id, chefId, waiterId, timeline, order, tableIndex); //create a new CustomerData object to store the customer data
            customerServingData.put((this.id - 1), customerData); //add the customer data to the customerServingTimeline
            Customer removed = (Customer) tableQueue.remove(tableIndex - 1); //remove the customer from the tableQueue
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

    //Helper methods -------------------------------------------------------------
    //This method is used to sort the customers by arrival time in the priority queue (customerArrivalQueue)
    @Override
    public int compareTo(Customer o) {
        //return the earliest arrival time, because the this.arrivalTime comes first, it will be sorted in ascending order
        return this.serveTime.compareTo(o.serveTime);
    }

    //This method is used to calculate the time difference between the operation start time and the current time
    private LocalTime getTimeDifference(LocalTime operationStart) {
        Duration duration = Duration.between(operationStart, LocalTime.now());
        double realTime = duration.toMillis() / (1000.0 * 60) / 0.1;
        return arrivalTime.plusMinutes((long)realTime);
    }

    //This method is used to generate a random eating time between 5 and 10 minutes
    public int generateRandomEatingTime() {
        Random random = new Random();
        int min = 5;
        int max = 10;
        int num = ((random.nextInt((max - min) + 1) + min));
        return num;
    }

}
