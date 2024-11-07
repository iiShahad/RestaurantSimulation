
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

//This class is a wrapper class
class CustomerData {

    private int customerId;
    private int chefId;
    private int waiterId;
    private HashMap<String, LocalTime> events;
    private Order order;
    private int tableIndex;

    public CustomerData(int customerId, int chefId, int waiterId, HashMap<String, LocalTime> events, Order order, int tableIndex) {
        this.customerId = customerId;
        this.chefId = chefId;
        this.waiterId = waiterId;
        this.events = events;
        this.order = order;
        this.tableIndex = tableIndex;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getChefId() {
        return chefId;
    }

    public int getWaiterId() {
        return waiterId;
    }

    public Order getOrder() {
        return order;
    }

    public HashMap<String, LocalTime> getEvents() {
        return events;
    }

    public static void printEvents(CustomerData customerData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        // Ensure we only print each event once
        if (customerData.events.containsKey("Arrival")) {
            System.out.printf("[%s] Customer %d arrives.%n", customerData.events.get("Arrival").format(formatter), customerData.customerId);
        }
        if (customerData.events.containsKey("Seated")) {
            System.out.printf("[%s] Customer %d is seated at Table %d%n", customerData.events.get("Seated").format(formatter), customerData.customerId, customerData.tableIndex);
        }
        if (customerData.events.containsKey("Order")) {
            System.out.printf("[%s] Customer %d places an order: %s%n", customerData.events.get("Order").format(formatter), customerData.customerId, customerData.order.getMealName());
        }
        if (customerData.events.containsKey("ChefStart")) {
            System.out.printf("[%s] Chef %d starts preparing %s for Customer %d%n", customerData.events.get("ChefStart").format(formatter), customerData.chefId, customerData.order.getMealName(), customerData.customerId);
        }
        if (customerData.events.containsKey("ChefFinish")) {
            System.out.printf("[%s] Chef %d finishes preparing %s for Customer %d%n", customerData.events.get("ChefFinish").format(formatter), customerData.chefId, customerData.order.getMealName(), customerData.customerId);
        }
        if (customerData.events.containsKey("Serve")) {
            System.out.printf("[%s] Waiter %d serves %s to Customer %d at Table %d%n", customerData.events.get("Serve").format(formatter), customerData.waiterId, customerData.order.getMealName(), customerData.customerId, customerData.tableIndex);
        }
        if (customerData.events.containsKey("Leave")) {
            System.out.printf("[%s] Customer %d finishes eating and leaves the restaurant.%n", customerData.events.get("Leave").format(formatter), customerData.customerId);
            System.out.printf("[%s] Table %d is now available.%n", customerData.events.get("Leave").format(formatter), customerData.tableIndex);
        }
    }
}
