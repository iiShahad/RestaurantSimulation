
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

//This class is a wrapper class, which stores the data of a customer, including the customer ID, chef ID, waiter ID, events, order, and table index.
class CustomerData {

    private final int customerId;
    private final int chefId;
    private final int waiterId;
    private final HashMap<String, LocalTime> events;
    private final Order order;
    private final int tableIndex;

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

    public String toString(CustomerData customerData) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        StringBuilder events = new StringBuilder();

        if (customerData.events.containsKey("Arrival")) {
            events.append(String.format("[%s] Customer %d arrives.%n",
                    customerData.events.get("Arrival").format(formatter), customerData.customerId));
        }
        if (customerData.events.containsKey("Seated")) {
            events.append(String.format("[%s] Customer %d is seated at Table %d%n",
                    customerData.events.get("Seated").format(formatter), customerData.customerId, customerData.tableIndex));
        }
        if (customerData.events.containsKey("Order")) {
            events.append(String.format("[%s] Customer %d places an order: %s%n",
                    customerData.events.get("Order").format(formatter), customerData.customerId, customerData.order.getMealName()));
        }
        if (customerData.events.containsKey("ChefStart")) {
            events.append(String.format("[%s] Chef %d starts preparing %s for Customer %d%n",
                    customerData.events.get("ChefStart").format(formatter), customerData.chefId,
                    customerData.order.getMealName(), customerData.customerId));
        }
        if (customerData.events.containsKey("ChefFinish")) {
            events.append(String.format("[%s] Chef %d finishes preparing %s for Customer %d%n",
                    customerData.events.get("ChefFinish").format(formatter), customerData.chefId,
                    customerData.order.getMealName(), customerData.customerId));
        }
        if (customerData.events.containsKey("Serve")) {
            events.append(String.format("[%s] Waiter %d serves %s to Customer %d at Table %d%n",
                    customerData.events.get("Serve").format(formatter), customerData.waiterId,
                    customerData.order.getMealName(), customerData.customerId, customerData.tableIndex));
        }
        if (customerData.events.containsKey("Leave")) {
            events.append(String.format("[%s] Customer %d finishes eating and leaves the restaurant.%n",
                    customerData.events.get("Leave").format(formatter), customerData.customerId));
            events.append(String.format("[%s] Table %d is now available.%n",
                    customerData.events.get("Leave").format(formatter), customerData.tableIndex));
        }

        return events.toString();
    }

}
